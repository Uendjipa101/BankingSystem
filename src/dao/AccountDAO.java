package dao;

import model.Account;
import model.ChequeAccount;
import model.EmploymentDetails;
import model.InvestmentAccount;
import model.SavingsAccount;
import model.Transaction;
import model.TransactionType;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    // CREATE
    public void insert(String customerId, Account account) {
        DatabaseConnection.transaction(c -> {
            String sql = "INSERT INTO accounts (account_number, customer_id, account_type, branch, "
                    + "date_opened, balance, last_interest_month) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, account.getAccountNumber());
                ps.setString(2, customerId);
                ps.setString(3, account.getAccountType().toUpperCase());
                ps.setString(4, account.getBranch());
                ps.setDate(5, Date.valueOf(account.getDateOpened()));
                ps.setDouble(6, account.getBalance());
                ps.setString(7, lastInterestOf(account));
                ps.executeUpdate();
            }
            saveEmployment(c, account);
            saveTransactions(c, account);
        });
    }

    // READ
    public Account findByAccountNumber(String accountNumber) {
        return queryOne("SELECT * FROM accounts WHERE account_number = ?", accountNumber);
    }

    public Account findByAccountNumber(String accountNumber, String customerId) {
        return queryOne("SELECT * FROM accounts WHERE account_number = ? AND customer_id = ?",
                accountNumber, customerId);
    }

    public List<Account> findByCustomer(String customerId) {
        return queryMany("SELECT * FROM accounts WHERE customer_id = ? ORDER BY account_number",
                customerId);
    }

    public List<Account> findAll() {
        return queryMany("SELECT * FROM accounts ORDER BY account_number");
    }

    public String nextAccountNumber() {
        String sql = "SELECT COALESCE(MAX(CAST(account_number AS BIGINT)), 1000) + 1 FROM accounts";
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return String.valueOf(rs.getLong(1));
        } catch (SQLException e) {
            throw new DataAccessException("Could not create an account number", e);
        }
    }

    // UPDATE
    public void update(Account account) {
        DatabaseConnection.transaction(c -> {
            String sql = "UPDATE accounts SET balance = ?, last_interest_month = ? WHERE account_number = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setDouble(1, account.getBalance());
                ps.setString(2, lastInterestOf(account));
                ps.setString(3, account.getAccountNumber());
                if (ps.executeUpdate() == 0) {
                    throw new DataAccessException("Account not found: " + account.getAccountNumber());
                }
            }
            saveTransactions(c, account);
        });
    }

    // DELETE
    public void delete(String accountNumber) {
        DatabaseConnection.transaction(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM accounts WHERE account_number = ?")) {
                ps.setString(1, accountNumber);
                ps.executeUpdate();
            }
        });
    }

    // HELPER METHODS
    private Account queryOne(String sql, String... params) {
        List<Account> list = queryMany(sql, params);
        return list.isEmpty() ? null : list.get(0);
    }

    private List<Account> queryMany(String sql, String... params) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Account> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(c, rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read accounts: " + e.getMessage(), e);
        }
    }

    private Account map(Connection c, ResultSet rs) throws SQLException {
        String number = rs.getString("account_number");
        String type = rs.getString("account_type");
        String branch = rs.getString("branch");
        LocalDate opened = rs.getDate("date_opened").toLocalDate();
        double balance = rs.getDouble("balance");
        String lastMonth = rs.getString("last_interest_month");
        List<Transaction> history = loadTransactions(c, number);

        return switch (type) {
            case "SAVINGS" -> new SavingsAccount(number, branch, opened, balance, history,
                    YearMonth.parse(lastMonth));
            case "INVESTMENT" -> new InvestmentAccount(number, branch, opened, balance, history,
                    YearMonth.parse(lastMonth));
            case "CHEQUE" -> new ChequeAccount(number, branch, opened, balance, history,
                    loadEmployment(c, number));
            default -> throw new DataAccessException("Unknown account type: " + type);
        };
    }

    private List<Transaction> loadTransactions(Connection c, String accountNumber) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE account_number = ? "
                + "ORDER BY LENGTH(transaction_id), transaction_id";
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getString("transaction_id"),
                            TransactionType.valueOf(rs.getString("txn_type")),
                            rs.getDouble("amount"),
                            rs.getTimestamp("txn_time").toLocalDateTime(),
                            rs.getDouble("balance_after"),
                            rs.getString("description")));
                }
            }
        }
        return list;
    }

    private EmploymentDetails loadEmployment(Connection c, String accountNumber) throws SQLException {
        String sql = "SELECT company_name, company_address FROM employment_details WHERE account_number = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new EmploymentDetails(rs.getString("company_name"),
                            rs.getString("company_address"));
                }
            }
        }
        throw new DataAccessException("Employment details missing for account " + accountNumber);
    }

    private void saveEmployment(Connection c, Account account) throws SQLException {
        if (account instanceof ChequeAccount cheque) {
            String sql = "MERGE INTO employment_details (account_number, company_name, company_address) "
                    + "KEY (account_number) VALUES (?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, account.getAccountNumber());
                ps.setString(2, cheque.getEmploymentDetails().getCompanyName());
                ps.setString(3, cheque.getEmploymentDetails().getCompanyAddress());
                ps.executeUpdate();
            }
        }
    }

    private void saveTransactions(Connection c, Account account) throws SQLException {
        String sql = "MERGE INTO transactions (transaction_id, account_number, txn_type, amount, "
                + "txn_time, balance_after, description) KEY (transaction_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Transaction t : account.getTransactions()) {
                ps.setString(1, t.getTransactionId());
                ps.setString(2, account.getAccountNumber());
                ps.setString(3, t.getType().name());
                ps.setDouble(4, t.getAmount());
                ps.setTimestamp(5, Timestamp.valueOf(t.getTimestamp()));
                ps.setDouble(6, t.getBalanceAfter());
                ps.setString(7, t.getDescription());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private String lastInterestOf(Account account) {
        if (account instanceof SavingsAccount s) {
            return s.getLastInterestMonth().toString();
        }
        if (account instanceof InvestmentAccount i) {
            return i.getLastInterestMonth().toString();
        }
        return null;
    }
}
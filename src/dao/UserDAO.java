package dao;

import model.Account;
import model.BankTeller;
import model.CompanyCustomer;
import model.Customer;
import model.IndividualCustomer;
import model.SystemAdministrator;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final AccountDAO accountDAO = new AccountDAO();

    // CREATE
    public void insert(User user) {
        DatabaseConnection.transaction(c -> {
            String kind = null;
            String idNumber = null;
            String companyName = null;
            String registrationNumber = null;
            String staffNumber = null;

            if (user instanceof IndividualCustomer ind) {
                kind = "INDIVIDUAL";
                idNumber = ind.getIdNumber();
            } else if (user instanceof CompanyCustomer co) {
                kind = "COMPANY";
                companyName = co.getCompanyName();
                registrationNumber = co.getRegistrationNumber();
            } else if (user instanceof SystemAdministrator admin) {
                staffNumber = admin.getStaffNumber();
            } else if (user instanceof BankTeller teller) {
                staffNumber = teller.getStaffNumber();
            }

            String sql = "INSERT INTO users (user_id, username, password_hash, user_role, customer_kind, "
                    + "active, first_name, surname, address, phone_number, id_number, company_name, "
                    + "registration_number, staff_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, user.getUserId());
                ps.setString(2, user.getUsername());
                ps.setString(3, user.getPasswordHash());
                ps.setString(4, user.getRole().name());
                ps.setString(5, kind);
                ps.setBoolean(6, user.isActive());
                ps.setString(7, user.getFirstname());
                ps.setString(8, user.getSurname());
                ps.setString(9, user.getAddress());
                ps.setString(10, user.getPhoneNumber());
                ps.setString(11, idNumber);
                ps.setString(12, companyName);
                ps.setString(13, registrationNumber);
                ps.setString(14, staffNumber);
                ps.executeUpdate();
            }
        });
    }

    // READ
    public User findByUsername(String username) {
        List<User> list = queryMany("SELECT * FROM users WHERE username = ?", username);
        return list.isEmpty() ? null : list.get(0);
    }

    public User findById(String userId) {
        List<User> list = queryMany("SELECT * FROM users WHERE user_id = ?", userId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<User> findAll() {
        return queryMany("SELECT * FROM users ORDER BY user_id");
    }

    public List<User> findAllCustomers() {
        return queryMany("SELECT * FROM users WHERE user_role = 'CUSTOMER' ORDER BY surname, first_name");
    }

    public int countUsers() {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Could not count users", e);
        }
    }

    public String nextUserId() {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(user_id, 2) AS INT)), 0) + 1 FROM users";
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return "U" + rs.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Could not create a user id", e);
        }
    }

    // UPDATE
    public void update(User user) {
        DatabaseConnection.transaction(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE users SET active = ? WHERE user_id = ?")) {
                ps.setBoolean(1, user.isActive());
                ps.setString(2, user.getUserId());
                if (ps.executeUpdate() == 0) {
                    throw new DataAccessException("User not found: " + user.getUserId());
                }
            }
        });
    }

    // ---------- DELETE ----------
    public void delete(String userId) {
        DatabaseConnection.transaction(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }
        });
    }

    // HELPER METHODS
    private List<User> queryMany(String sql, String... params) {
        List<User> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read users: " + e.getMessage(), e);
        }
        for (User user : result) {
            if (user instanceof Customer customer) {
                for (Account account : accountDAO.findByCustomer(user.getUserId())) {
                    customer.addAccount(account);
                }
            }
        }
        return result;
    }

    private User map(ResultSet rs) throws SQLException {
        String id = rs.getString("user_id");
        String username = rs.getString("username");
        String hash = rs.getString("password_hash");
        String first = rs.getString("first_name");
        String surname = rs.getString("surname");
        String address = rs.getString("address");
        String phone = rs.getString("phone_number");

        User user = switch (rs.getString("user_role")) {
            case "ADMINISTRATOR" -> new SystemAdministrator(id, username, hash, first, surname,
                    address, phone, rs.getString("staff_number"));
            case "TELLER" -> new BankTeller(id, username, hash, first, surname,
                    address, phone, rs.getString("staff_number"));
            default -> "COMPANY".equals(rs.getString("customer_kind"))
                    ? new CompanyCustomer(id, username, hash, first, surname, address, phone,
                    rs.getString("company_name"), rs.getString("registration_number"))
                    : new IndividualCustomer(id, username, hash, first, surname, address, phone,
                    rs.getString("id_number"));
        };
        if (!rs.getBoolean("active")) {
            user.deactivate();
        }
        return user;
    }
}

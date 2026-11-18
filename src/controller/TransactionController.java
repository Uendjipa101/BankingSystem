package controller;

import dao.AccountDAO;
import model.*;

import java.util.List;

public class TransactionController {
    private final AccountDAO accounts = new AccountDAO();
    private final Session session;

    public TransactionController(Session session) {
        this.session = session;
    }

    public List<Account> myAccounts() {
        User user = session.requireRole(Role.CUSTOMER);
        return accounts.findByCustomer(user.getUserId());
    }

    public Account getAccount(String accountNumber) {
        User user = session.requireRole(Role.CUSTOMER);
        Account account = accounts.findByAccountNumber(accountNumber, user.getUserId());
        if (account == null) {
            throw new BankException("Account not found");
        }
        return account;
    }

    public Account deposit(String accountNumber, double amount) {
        checkAmount(amount);
        Account account = getAccount(accountNumber);
        account.deposit(amount, "Deposit");
        accounts.update(account);
        return account;
    }

    public Account withdraw(String accountNumber, double amount) {
        checkAmount(amount);
        Account account = getAccount(accountNumber);
        if (!(account instanceof Withdrawable withdrawable)) {
            throw new BankException("Withdrawals are not allowed from a " + account.getAccountType() + " account");
        }
        if (!withdrawable.withdraw(amount)) {
            throw new BankException("Insufficient funds");
        }
        accounts.update(account);
        return account;
    }

    public List<Transaction> history(String accountNumber) {
        return getAccount(accountNumber).getTransactions();
    }

    private void checkAmount(double amount) {
        if (amount <= 0) {
            throw new BankException("Amount must be greater than zero");
        }
    }
}
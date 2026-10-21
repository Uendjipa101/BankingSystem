package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Account {
    private final String accountNumber;
    private double balance;
    private final String branch;
    private final LocalDate dateOpened;
    private final List<Transaction> transactions = new ArrayList<>();

    protected Account(String accountNumber,String branch, LocalDate dateOpened) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number is required.");
        }
        if (branch == null || branch.isBlank()) {
            throw new IllegalArgumentException("Branch is required.");
        }
        this.accountNumber = accountNumber;
        this.branch = branch;
        this.dateOpened = dateOpened;
        this.balance = 0.0;
    }

    public final void deposit(double amount) {
        deposit(amount, "Deposit");
    }
    public final void deposit(double amount, String description) {
        if(amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        }
        credit(amount);
        recordTransaction(TransactionType.DEPOSIT, amount, description);
    }

    protected void credit(double amount) {balance += amount;}
    protected void debit(double amount) {balance -= amount;}

    protected void recordTransaction(TransactionType type, double amount, String description) {
        String id = accountNumber + "-T" + (transactions.size() + 1);
        transactions.add(new Transaction(id, type, amount,balance, description));
    }

    public abstract String getAccountType();

    public String getAccountNumber() {
        return accountNumber;
    }
    public double getBalance() {
        return balance;
    }
    public String getBranch() {
        return branch;
    }
    public LocalDate getDateOpened() {
        return dateOpened;
    }
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    @Override
    public String toString() {
        return getAccountType() + " " + accountNumber
        + " - BWP " + String.format("%.2f", balance);
    }
}

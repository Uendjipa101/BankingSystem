package model;
import java.time.LocalDateTime;

public class Transaction {
    private final String transactionId;
    private final TransactionType type;
    private final double amount;
    private final LocalDateTime timestamp;
    private final double balanceAfter;
    private final String description;

    public Transaction(String transactionId, TransactionType type, double amount, double balanceAfter, String description) {
        this(transactionId, type, amount, LocalDateTime.now(), balanceAfter, description);
    }

    public Transaction(String transactionId, TransactionType type, double amount, LocalDateTime timestamp, double balanceAfter, String description) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }
}

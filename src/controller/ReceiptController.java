package controller;

import model.Account;
import model.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReceiptController {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String transactionReceipt(Account account, Transaction t) {
        StringBuilder sb = new StringBuilder();
        sb.append("========== BANK RECEIPT ==========\n");
        sb.append(String.format("Account : %s (%s)%n", account.getAccountNumber(), account.getAccountType()));
        sb.append(String.format("Branch  : %s%n", account.getBranch()));
        sb.append(String.format("Date    : %s%n", t.getTimestamp().format(TIME)));
        sb.append(String.format("Type    : %s%n", t.getType()));
        sb.append(String.format("Amount  : BWP %.2f%n", t.getAmount()));
        sb.append(String.format("Balance : BWP %.2f%n", t.getBalanceAfter()));
        sb.append(String.format("Ref     : %s%n", t.getTransactionId()));
        sb.append("==================================\n");
        return sb.toString();
    }

    public String latestReceipt(Account account) {
        List<Transaction> list = account.getTransactions();
        if (list.isEmpty()) {
            throw new BankException("There are no transactions to print");
        }
        return transactionReceipt(account, list.get(list.size() - 1));
    }

    public String historyStatement(Account account) {
        StringBuilder sb = new StringBuilder();
        sb.append("====== TRANSACTION HISTORY ======\n");
        sb.append(String.format("Account : %s (%s)%n", account.getAccountNumber(), account.getAccountType()));
        sb.append(String.format("Balance : BWP %.2f%n%n", account.getBalance()));
        sb.append(String.format("%-16s %-11s %10s %12s%n", "Date", "Type", "Amount", "Balance"));
        for (Transaction t : account.getTransactions()) {
            sb.append(String.format("%-16s %-11s %10.2f %12.2f%n",
                    t.getTimestamp().format(TIME), t.getType(), t.getAmount(), t.getBalanceAfter()));
        }
        sb.append("==================================\n");
        return sb.toString();
    }

    public Path saveToFile(String text, String fileName) {
        try {
            Path folder = Path.of("receipts");
            Files.createDirectories(folder);
            return Files.writeString(folder.resolve(fileName + ".txt"), text);
        } catch (IOException e) {
            throw new BankException("Could not save the receipt: " + e.getMessage());
        }
    }
}
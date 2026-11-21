package view;

import controller.BankException;
import controller.ReceiptController;
import controller.Session;
import controller.TransactionController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.*;

import java.util.List;

public class CustomerView {
    private final BankApp app;
    private final Session session;
    private final TransactionController transactions;
    private final ReceiptController receipts = new ReceiptController();
    private final TableView<Account> accountTable = new TableView<>();
    private final TableView<Transaction> historyTable = new TableView<>();
    private final TextField amountField = new TextField();
    private final CheckBox receiptBox = new CheckBox("Print Receipt");

    public CustomerView(BankApp app, Session session){
        this.app = app;
        this.session = session;
        this.transactions = new TransactionController(session);
    }

    public Parent build(){
        BorderPane root = new BorderPane();
        root.setTop(Ui.header("My Accounts", session.getCurrentUser().getDisplayName(), app::logout));

        accountTable.getColumns().add(Ui.<Account>column("Account", 120, Account::getAccountNumber));
        accountTable.getColumns().add(Ui.<Account>column("Type", 110, Account::getAccountType));
        accountTable.getColumns().add(Ui.<Account>column("Branch", 120, Account::getBranch));
        accountTable.getColumns().add(Ui.<Account>column("Balance", 140, a -> Ui.money(a.getBalance())));
        accountTable.setPrefHeight(170);
        accountTable.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> showHistory(now));

        amountField.setPromptText("Amount (BWP)");
        Button deposit = Ui.button("Deposit");
        deposit.setOnAction(e -> deposit());
        Button withdraw = Ui.button("Withdraw");
        withdraw.setOnAction(e -> withdraw());
        HBox actions = new HBox(10, amountField, deposit, withdraw, receiptBox);
        actions.setAlignment(Pos.CENTER_LEFT);

        historyTable.getColumns().add(Ui.<Transaction>column("Date", 150, t -> t.getTimestamp().format(Ui.TIME)));
        historyTable.getColumns().add(Ui.<Transaction>column("Type", 110, t -> t.getType().name()));
        historyTable.getColumns().add(Ui.<Transaction>column("Amount", 110, t -> String.format("%.2f", t.getAmount())));
        historyTable.getColumns().add(Ui.<Transaction>column("Balance after", 130, t -> String.format("%.2f", t.getBalanceAfter())));
        historyTable.getColumns().add(Ui.<Transaction>column("Description", 220, Transaction::getDescription));
        VBox.setVgrow(historyTable, Priority.ALWAYS);

        Button statement = Ui.button("Print statement");
        statement.setOnAction(e -> printStatement());

        VBox center = new VBox(10, Ui.section("Select an account"), accountTable, actions,
                Ui.section("Transaction history"), historyTable, statement);
        center.setPadding(new Insets(16));
        root.setCenter(center);

        Ui.run(() -> refresh(null));
        return root;
    }
    private Account selected(){
        Account account = accountTable.getSelectionModel().getSelectedItem();
        if(account == null){
            throw new BankException("Select an account first");
        }
        return account;
    }

    private void deposit(){
        Ui.run(() ->{
            Account account = selected();
            double amount = Ui.parseAmount(amountField.getText());
            afterTransaction(transactions.deposit(account.getAccountNumber(), amount), "Deposit successful");
        });
    }

    private void withdraw(){
        Ui.run(() ->{
            Account account = selected();
            double amount = Ui.parseAmount(amountField.getText());
            afterTransaction(transactions.withdraw(account.getAccountNumber(), amount), "Withdraw successful");
        });
    }

    private void afterTransaction(Account updated, String message) {
        amountField.clear();
        refresh(updated.getAccountNumber());
        if (receiptBox.isSelected()) {
            Ui.showText(receipts, "Receipt", receipts.latestReceipt(updated),
                    "receipt-" + updated.getAccountNumber() + "-" + System.currentTimeMillis());
        } else {
            Ui.info(message + " New balance: " + Ui.money(updated.getBalance()));
        }
    }

    private void printStatement() {
        Ui.run(() -> {
            Account account = transactions.getAccount(selected().getAccountNumber());
            Ui.showText(receipts, "Statement", receipts.historyStatement(account),
                    "statement-" + account.getAccountNumber() + "-" + System.currentTimeMillis());
        });
    }

    private void refresh(String keepAccountNumber) {
        List<Account> list = transactions.myAccounts();
        accountTable.setItems(FXCollections.observableArrayList(list));
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getAccountNumber().equals(keepAccountNumber)) {
                index = i;
            }
        }
        if (!list.isEmpty()) {
            accountTable.getSelectionModel().clearSelection();
            accountTable.getSelectionModel().select(index);
        }
    }

    private void showHistory(Account account) {
        if (account == null) {
            historyTable.getItems().clear();
            return;
        }
        Ui.run(() -> historyTable.setItems(
                FXCollections.observableArrayList(transactions.history(account.getAccountNumber()))));
    }
}

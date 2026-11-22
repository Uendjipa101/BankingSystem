import controller.*;
import dao.DatabaseConnection;
import dao.SampleData;
import model.*;

import java.util.List;
import java.util.Scanner;
import java.util.NoSuchElementException;

public class ConsoleApp {
    public static final Scanner in = new Scanner(System.in);
    private static final Session session = new Session();
    private static final LoginController login = new LoginController(session);
    private static final ReceiptController receipts = new ReceiptController();

    public static void main(String[] args) {
        DatabaseConnection.initialise();
        SampleData.loadIfEmpty();
        int paid = new InterestController().payDueInterest();
        System.out.println("==== BANKING SYSTEM ====");
        if (paid > 0) {
            System.out.println(paid + " monthly interest payment(s) credited at startup");
        }
        try{
            boolean running = true;
            while (running) {
                if (!session.isLoggedIn()) {
                    running = loginScreen();
                } else {
                    switch(session.getCurrentUser().getRole()) {
                        case CUSTOMER -> customerMenu();
                        case TELLER -> tellerMenu();
                        case ADMINISTRATOR -> adminMenu();
                    }
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println();
        }
        System.out.println("Goodbye!");
    }
    // --- helpers ---
    private static String ask(String label){
        System.out.print(label + ": ");
        return in.nextLine().trim();
    }

    private static double amount(String text){
        try{
            return Double.parseDouble(text.replace(",", "."));
        } catch (NumberFormatException e){
            throw new IllegalArgumentException("Enter a valid amount, for example 250.00");
        }
    }
    private static String money(double value){
        return String.format("BWP %.2f", value);
    }

    // --- login ---
    private static boolean loginScreen() {
        System.out.println("\n --- Log in (type q as the username to quit) ---");
        String username = ask("Username: ");
        if (username.equalsIgnoreCase("q")) {
            return false;
        }
        String password = ask("Password: ");
        try{
            User user = login.login(username, password);
            System.out.println("Welcome, " + user.getDisplayName() + " [" + user.getRole() + "]");
        } catch (RuntimeException e){
            System.out.println("Error: " + e.getMessage());
        }
        return true;
    }

    // --- customer ---
    private static void customerMenu() {
        TransactionController tx = new TransactionController(session);
        System.out.println("\n[Customer]  1 My accounts   2 Deposit   3 Withdraw   4 Transaction history   0 Log out");
        String choice = ask("Choose");
        try {
            switch (choice) {
                case "1" -> {
                    for (Account a : tx.myAccounts()) {
                        System.out.println("  " + a);
                    }
                }
                case "2" -> moveMoney(tx, true);
                case "3" -> moveMoney(tx, false);
                case "4" -> System.out.print(receipts.historyStatement(tx.getAccount(ask("Account number"))));
                case "0" -> login.logout();
                default -> System.out.println("Unknown choice.");
            }
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void moveMoney(TransactionController tx, boolean deposit) {
        String number = ask("Account number");
        double value = amount(ask("Amount (BWP)"));
        Account account = deposit ? tx.deposit(number, value) : tx.withdraw(number, value);
        System.out.println((deposit ? "Deposit" : "Withdrawal") + " successful. New balance: "
                + money(account.getBalance()));
        if (ask("Print receipt? (y/n)").equalsIgnoreCase("y")) {
            System.out.print(receipts.latestReceipt(account));
        }
    }
    // --- bank teller ---
    private static void tellerMenu() {
        RegistrationController reg = new RegistrationController(session);
        System.out.println("\n[Bank Teller] 1 Register individual 2 Register company   "
        + "3 Open account 4 List customers 0 Log out");
        String choice = ask("Choose");
        try {
            switch (choice) {
                case "1" -> {
                    Customer c = reg.registerIndividual(ask("Username"), ask("Password (min 6)"),
                            ask("First name"), ask("Surname"), ask("Address"), ask("Phone"),
                            ask("ID number"));
                    System.out.println("Registered " + c.getDisplayName() + " (id " + c.getUserId() + ")");
                }
                case "2" -> {
                    Customer c = reg.registerCompany(ask("Username"), ask("Password (min 6)"),
                            ask("Contact first name"), ask("Contact surname"), ask("Address"),
                            ask("Phone"), ask("Company name"), ask("Registration number"));
                    System.out.println("Registered " + c.getDisplayName() + " (id " + c.getUserId() + ")");
                }
                case "3" -> openAccount(reg);
                case "4" -> {
                    for (User u : reg.listCustomers()){
                        System.out.println(" " + u.getUserId() + " " + u.getUsername() + " " + u.getDisplayName());
                    }
                }case "0" -> login.logout();
                default -> System.out.println("Unknown choice.");
            }

        }catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
    }
    }
    private static void openAccount(RegistrationController reg) {
        String id = ask("Customer ID (for example U3)");
        String type = ask("Account type (1 Savings, 2 Investment, 3 Cheque)");
        String branch = ask("Branch");
        Account account;
        switch (type) {
            case "2" -> account = reg.openInvestment(id, branch, amount(ask("Opening deposit (BWP)")));
            case "3" -> account = reg.openCheque(id, branch, ask("Employer company name"),
                    ask("Employer company address"));
            default -> account = reg.openSavings(id, branch);
        }
        System.out.println("Account opened: " + account);
    }

    //--- administrator ---
    private static void adminMenu() {
        AdminController admin = new AdminController(session);
        System.out.println("\n[Administrator]  1 List users   2 Activate user   3 Deactivate user   "
                + "4 Create staff user   5 System activity   0 Log out");
        String choice = ask("Choose");
        try {
            switch (choice) {
                case "1" -> {
                    for (User u : admin.listUsers()) {
                        System.out.println("  " + u.getUserId() + "  " + u.getUsername() + "  "
                                + u.getDisplayName() + "  " + u.getRole() + "  "
                                + (u.isActive() ? "Active" : "Deactivated"));
                    }
                }
                case "2" -> {
                    admin.activateUser(ask("User id"));
                    System.out.println("User activated.");
                }
                case "3" -> {
                    admin.deactivateUser(ask("User id"));
                    System.out.println("User deactivated.");
                }
                case "4" -> {
                    Role role = ask("Role (1 Teller, 2 Administrator)").equals("2") ? Role.ADMINISTRATOR : Role.TELLER;
                    User u = admin.createStaff(role, ask("Username"), ask("Password (min 6)"),
                            ask("First name"), ask("Surname"), ask("Address"), ask("Phone"),
                            ask("Staff number"));
                    System.out.println("Created " + u.getRole() + " " + u.getDisplayName());
                }
                case "5" -> {
                    List<AdminController.ActivityEntry> entries = admin.activity();
                    int shown = Math.min(15, entries.size());
                    for (int i = 0; i < shown; i++) {
                        AdminController.ActivityEntry e = entries.get(i);
                        System.out.printf("  %s  %s  %-10s %10.2f%n", e.transaction().getTimestamp().toLocalDate(),
                                e.accountNumber(), e.transaction().getType(), e.transaction().getAmount());
                    }
                    System.out.println("  (" + shown + " of " + entries.size() + " transactions shown)");
                }
                case "0" -> login.logout();
                default -> System.out.println("Unknown choice.");
            }
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

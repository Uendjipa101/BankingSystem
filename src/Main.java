import controller.*;
import dao.DatabaseConnection;
import dao.SampleData;
import model.*;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection.initialise();
        SampleData.loadIfEmpty();
        System.out.println("Interest entries credited: " + new InterestController().payDueInterest());

        Session session = new Session();
        LoginController login = new LoginController(session);

        login.login("neo", "password123");
        TransactionController tx = new TransactionController(session);
        for (Account a : tx.myAccounts()) System.out.println("  " + a);
        String savings = tx.myAccounts().get(0).getAccountNumber();
        Account acc = tx.deposit(savings, 500);
        System.out.print(new ReceiptController().latestReceipt(acc));
        try { tx.withdraw(savings, 10); } catch (BankException e) { System.out.println(e.getMessage()); }
        login.logout();

        login.login("teller", "password123");
        RegistrationController reg = new RegistrationController(session);
        IndividualCustomer c = reg.registerIndividual("demo" + System.currentTimeMillis() % 10000,
                "secret1", "Demo", "Person", "Plot 1", "70000000", "999999999");
        System.out.println("Opened " + reg.openInvestment(c.getUserId(), "Gaborone", 600));
    }
}
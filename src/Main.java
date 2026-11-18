import dao.AccountDAO;
import dao.DatabaseConnection;
import dao.SampleData;
import dao.UserDAO;
import model.Account;
import model.Customer;
import model.InterestBearing;
import model.User;

import java.time.YearMonth;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection.initialise();
        SampleData.loadIfEmpty();

        UserDAO users = new UserDAO();
        User u = users.findByUsername("neo");
        System.out.println(u.getDisplayName() + " logs in: " + u.authenticate("password123"));

        Customer neo = (Customer) u;
        for (Account a : neo.getAccounts()) {
            System.out.println("  " + a);
        }

        AccountDAO accounts = new AccountDAO();
        Account savings = neo.getAccounts().get(0);
        savings.deposit(250);
        accounts.update(savings);

        Account reloaded = accounts.findByAccountNumber(savings.getAccountNumber());
        System.out.println("Reloaded: " + reloaded + " (" + reloaded.getTransactions().size() + " transactions)");

        if (reloaded instanceof InterestBearing ib) {
            ib.payInterest(YearMonth.now());
            accounts.update(reloaded);
            System.out.println("After interest: " + reloaded);
        }
        System.out.println(users.findAllCustomers().size() + " customers in the database");
    }
}
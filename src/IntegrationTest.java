import controller.AdminController;
import controller.InterestController;
import controller.LoginController;
import controller.ReceiptController;
import controller.RegistrationController;
import controller.Session;
import controller.TransactionController;
import dao.AccountDAO;
import dao.DatabaseConnection;
import dao.SampleData;
import dao.UserDAO;
import model.Account;
import model.CompanyCustomer;
import model.IndividualCustomer;
import model.Role;

public class IntegrationTest {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition){
        if (condition){
            passed++;
            System.out.println("PASS     " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }

    private static void refused(String name, Runnable action){
        try {
            action.run();
            check(name + " (should have been refused)", false);
        } catch (RuntimeException e) {
            check(name + " -> \"" + e.getMessage() + "\"", false);
        }
    }

    private static boolean near(double actual, double expected){
        return Math.abs(actual - expected) < 0.001;
    }

    public static void main(String[] args) {
        DatabaseConnection.initialise();
        SampleData.loadIfEmpty();

        UserDAO users = new UserDAO();
        AccountDAO accounts = new AccountDAO();
        String stamp = String.valueOf(System.currentTimeMillis() % 1000000);

        System.out.println("--- Database ---");
        check("Database holds at least 10 sample customers", users.findAllCustomers().size() >=10);
        check("Datebase holds at leat 10 accounts", accounts.findAll().size() >=10);

        System.out.println("--- Monthly Interest ---");
        InterestController interest = new InterestController();
        interest.payDueInterest();
        check("Interest is never paid twice for the same month", interest.payDueInterest() == 0);

        Session session = new Session();
        LoginController login = new LoginController(session);

        System.out.println("--- Login and roles ---");
        refused("Wrong password rejected", () -> login.login("neo", "wrong"));
        refused("Unknown user rejected", () -> login.login("nobody", "password123"));
        check("Customer logs in with the correct password",
                login.login("neo", "password123").getRole() == Role.CUSTOMER);
        refused("A customer cannot register customers", () ->
                new RegistrationController(session).registerIndividual("x" + stamp, "secret1",
                        "A", "B", "C", "D", "E"));
        login.logout();
        refused("Nobody logged in cannot view accounts", () -> new TransactionController(session).myAccounts());

        System.out.println("--- Bank teller: registration and account opening ---");
        login.login("teller", "password123");
        RegistrationController reg = new RegistrationController(session);
        String uname = "t" + stamp;
        IndividualCustomer person = reg.registerIndividual(uname, "secret1", "Test", "Person",
                "Plot 1", "70000000", "123123123");
        check("Teller registers an individual customer", users.findByUsername(uname) != null);
        refused("Duplicate username refused", () ->
                reg.registerIndividual(uname, "secret1", "A", "B", "C", "D", "E"));
        Account savings = reg.openSavings(person.getUserId(), "Gaborone");
        Account investment = reg.openInvestment(person.getUserId(),"Gaborone", 600);
        Account cheque = reg.openCheque(person.getUserId(), "Gaborone", "Acme Ltd", "Plot 2");
        check("Savings, Investment and Cheque for one customer",
                accounts.findByCustomer(person.getUserId()).size() == 3);
        refused("Investment account below BWP500.00 refused", () ->
                reg.openInvestment(person.getUserId(), "Gaborone", 499));
        CompanyCustomer company = reg.registerCompany(uname + "c", "secret1", "Con", "Tact",
                "Plot 3", "70000001", "Test Co", "BW" + stamp);
        reg.openSavings(company.getUserId(), "Gaborone");
        refused("A company cannot hold a Cheque account", () ->
                reg.openCheque(company.getUserId(), "Gaborone", "Acme", "Plot 2"));
        login.logout();

        System.out.println("--- Customer: transactions, receipts, history ---");
        login.login(uname, "secret1");
        TransactionController tx = new TransactionController(session);
        ReceiptController receipts = new ReceiptController();
        Account afterDeposit = tx.deposit(savings.getAccountNumber(), 1000);
        check("Deposit raises the Savings balance to 1000.00", near(afterDeposit.getBalance(), 1000.0));
        check("A receipt can be produced for the deposit",
                receipts.latestReceipt(afterDeposit).contains("BANK RECEIPT"));
        refused("Withdrawal from a Savings account refused", () -> tx.withdraw(savings.getAccountNumber(), 10));
        refused("Negative deposit refused", () -> tx.deposit(savings.getAccountNumber(), -5));
        check("Withdrawal from the Investment account succeeds",
                near(tx.withdraw(investment.getAccountNumber(), 100).getBalance(), 500.0));
        refused("Withdrawal above the balance refused", () -> tx.withdraw(investment.getAccountNumber(), 100000));
        tx.deposit(cheque.getAccountNumber(), 2000);
        check("Withdrawal from the Cheque account succeeds",
                near(tx.withdraw(cheque.getAccountNumber(), 300).getBalance(), 1700.0));
        check("Transaction history lists every transaction",
                tx.history(cheque.getAccountNumber()).size() == 2);
        check("Saved balance survives a reload from the database",
                near(accounts.findByAccountNumber(investment.getAccountNumber()).getBalance(), 500.0));
        login.logout();

        login.login("neo", "password123");
        TransactionController neoTx = new TransactionController(session);
        refused("A customer cannot use another customer's account", () ->
                neoTx.deposit(savings.getAccountNumber(), 10));
        login.logout();

        System.out.println("--- Administrator ---");
        login.login("admin", "password123");
        AdminController admin = new AdminController(session);
        admin.deactivateUser(person.getUserId());
        login.logout();
        refused("A deactivated user cannot log in", () -> login.login(uname, "secret1"));
        login.login("admin", "password123");
        admin.activateUser(person.getUserId());
        check("Administrator can see system activity", !admin.activity().isEmpty());
        String adminId = session.getCurrentUser().getUserId();
        refused("Administrator cannot deactivate themselves", () -> admin.deactivateUser(adminId));
        login.logout();
        check("A reactivated user can log in again", login.login(uname, "secret1") != null);
        login.logout();

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }

    }
}

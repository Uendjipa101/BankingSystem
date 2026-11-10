import model.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class Main {
    public static void main(String[] args) {
        IndividualCustomer neo = new IndividualCustomer("U1", "neo", User.hashPassword("pass123"),
                "Neo", "Kgosi", "Plot 5, Gaborone", "71234567", "123456789");
        CompanyCustomer acme = new CompanyCustomer("U2", "acme", User.hashPassword("secret"),
                "Thato", "Moeng", "Plot 9, Gaborone", "72345678", "Acme Ltd", "BW000123");

        System.out.println(neo.getDisplayName() + " | " + acme.getDisplayName());
        System.out.println(neo.authenticate("pass123") + " " + neo.authenticate("wrong")); // true false

        ChequeAccount cheque = new ChequeAccount("3001", "Gaborone", LocalDate.now(),
                new EmploymentDetails("Acme Ltd", "Plot 12, Gaborone"));
        neo.addAccount(cheque);                       // allowed
        try {
            acme.addAccount(cheque);                  // must throw
        } catch (IllegalArgumentException e) {
            System.out.println("Blocked: " + e.getMessage());
        }
        neo.deactivate();
        System.out.println(neo.isActive());         // false
    }
    }


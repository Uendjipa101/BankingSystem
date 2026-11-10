import model.ChequeAccount;
import model.EmploymentDetails;
import model.InvestmentAccount;
import java.time.LocalDate;
import java.time.YearMonth;

public class Main {
    public static void main(String[] args) {
        ChequeAccount c = new ChequeAccount("3001", "Gaborone", LocalDate.of(2026, 10, 15),
                new EmploymentDetails("Acme Ltd", "Plot 12, Gaborone"));
        c.deposit(2000);
        System.out.println(c.withdraw(500));    // true  -> balance 1500.00
        System.out.println(c.withdraw(5000));   // false -> balance stays 1500.00
        System.out.println(c);                  // Cheque 3001 - BWP 1500.00
    }
}
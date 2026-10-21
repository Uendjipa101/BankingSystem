import model.InvestmentAccount;
import java.time.LocalDate;
import java.time.YearMonth;

public class Main {
    public static void main(String[] args) {
        InvestmentAccount inv = new InvestmentAccount("2001", "Gaborone", LocalDate.of(2026, 10, 15), 499);
        System.out.println(inv);                       // balance 500.00
        System.out.println(inv.withdraw(100));         // true  -> balance 400.00
        System.out.println(inv.withdraw(1000));        // false -> balance still 400.00
        inv.deposit(50);                               // allowed: the minimum is only for opening
        inv.payInterest(YearMonth.of(2027, 1));        // two months, the second bigger than the first
    }
}
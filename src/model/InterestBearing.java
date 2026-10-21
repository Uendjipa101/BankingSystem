package model;
import java.time.YearMonth;

public interface InterestBearing {
    double getInterestRate();
    double calculateInterest();
    void payInterest(YearMonth currentMonth);

}

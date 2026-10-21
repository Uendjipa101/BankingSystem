package model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class SavingsAccount extends Account implements InterestBearing{
    private final double INTEREST_RATE = 0.0005;
    private YearMonth lastInterestMonth;

    public SavingsAccount(String accountNumber,String branch, LocalDate dateOpened) {
        super(accountNumber,branch, dateOpened);
        this.lastInterestMonth = YearMonth.from(dateOpened);
    }
    @Override
    public double getInterestRate() {
        return INTEREST_RATE;
    }
    public double calculateInterest() {
        double interest = getBalance() * INTEREST_RATE;
        return Math.round(interest * 100.0) / 100.0;
    }

    @Override
    public void payInterest(YearMonth currentMonth) {
        YearMonth lastCompleted = currentMonth.minusMonths(1);
        while(lastInterestMonth.isBefore(lastCompleted)){
            lastInterestMonth = lastInterestMonth.plusMonths(1);
            double interest = calculateInterest();
            if(interest > 0){
                credit(interest);
                recordTransaction(TransactionType.INTEREST, interest, "Interest for " + lastInterestMonth);
            }
        }
    }

    public YearMonth getLastInterestMonth() {
        return lastInterestMonth;
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }
}

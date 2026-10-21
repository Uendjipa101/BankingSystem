package model;

import java.time.LocalDate;
import java.time.YearMonth;

public class InvestmentAccount extends Account implements InterestBearing, Withdrawable{
    public final double INTEREST_RATE = 0.05;
    private static final double MIN_OPENING_DEPOSIT = 500.00;
    private YearMonth lastInterestMonth;

    public InvestmentAccount(String accountNumber,String branch, LocalDate dateOpened, double initialDeposit) {
        super(accountNumber, branch, dateOpened);
        if (initialDeposit < MIN_OPENING_DEPOSIT) {
            throw new IllegalArgumentException("An Investment account needs an opening deposit of at least BWP500.00");
        }
        this.lastInterestMonth = YearMonth.from(dateOpened);
        deposit(initialDeposit, "Opening deposit");
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

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        }
        if (amount > getBalance()) {
            return false;
        }
        debit(amount);
        recordTransaction(TransactionType.WITHDRAWAL, amount, "Withdrawal");
        return true;
    }
    public YearMonth getLastInterestMonth() {
        return lastInterestMonth;
    }

    @Override
    public String getAccountType() {
        return "Investment";
    }
}

package model;

import java.time.LocalDate;
import java.util.List;

public class ChequeAccount extends Account implements Withdrawable{
    private final EmploymentDetails employmentDetails;

    public ChequeAccount(String accountNumber, String branch, LocalDate dateopened,EmploymentDetails employmentDetails) {
        super(accountNumber, branch, dateopened);
        if(employmentDetails == null){
            throw new IllegalArgumentException(
                    "A cheque account requires the customer's employment details"
            );
        }
        this.employmentDetails = employmentDetails;
    }

    public ChequeAccount(String accountNumber, String branch, LocalDate dateOpened, double balance, List<Transaction> history, EmploymentDetails employmentDetails) {
        super(accountNumber, branch, dateOpened, balance, history);
        if (employmentDetails == null){
            throw new IllegalArgumentException("A Cheque account requires the customer's employment details");

        }
        this.employmentDetails = employmentDetails;
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount < 0){
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }
        if (amount > getBalance()){
            return false;
        }
        debit(amount);
        recordTransaction(TransactionType.WITHDRAWAL, amount, "Withdrawal");
        return true;
    }
    public EmploymentDetails getEmploymentDetails() {
        return employmentDetails;
    }

    @Override
    public String getAccountType() {
        return "Cheque";
    }
}

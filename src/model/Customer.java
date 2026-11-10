package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Customer extends User {
    private final List<Account> accounts = new ArrayList<>();
    protected Customer(String userId, String username, String passwordHash, String firstName, String surname, String address, String phoneNumber){
        super(userId, username, passwordHash, Role.CUSTOMER, firstName, surname, address, phoneNumber);
    }

    public List<Account> getAccounts(){
        return Collections.unmodifiableList(accounts);
    }

    public void addAccount(Account account){
        if(account == null){
            throw new IllegalArgumentException("Account is required");
        }
        if(!canHold(account)){
            throw new IllegalArgumentException("This type of customer cannot hold a " + account.getAccountType() + " account");
        }
        for (Account existing : accounts){
            if (existing.getAccountType().equals(account.getAccountNumber())){
                throw new IllegalArgumentException("This customer already has that account");
            }
        }
        accounts.add(account);
    }
    protected boolean canHold(Account account){
        return true;
    }
}

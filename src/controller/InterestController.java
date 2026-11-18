package controller;

import dao.AccountDAO;
import model.*;

import java.time.YearMonth;

public class InterestController {
    private final AccountDAO accounts = new AccountDAO();

    public int payDueInterest() {
        return payDueInterest(YearMonth.now());
    }

    public int payDueInterest(YearMonth currentMonth) {
        int credited = 0;
        for (Account account : accounts.findAll()) {
            if (account instanceof InterestBearing earner) {
                int before = account.getTransactions().size();
                earner.payInterest(currentMonth);
                credited += account.getTransactions().size() - before;
                accounts.update(account);
            }
        }
        return credited;
    }
}

package controller;

import dao.AccountDAO;
import dao.UserDAO;
import model.Account;
import model.BankTeller;
import model.Role;
import model.SystemAdministrator;
import model.Transaction;
import model.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AdminController {
    public record ActivityEntry(String accountNumber, Transaction transaction) { }

    private final UserDAO users = new UserDAO();
    private final AccountDAO accounts = new AccountDAO();
    private final Session session;

    public AdminController(Session session) {
        this.session = session;
    }

    public List<User> listUsers() {
        session.requireRole(Role.ADMINISTRATOR);
        return users.findAll();
    }

    public void activateUser(String userId) {
        setActive(userId, true);
    }

    public void deactivateUser(String userId) {
        setActive(userId, false);
    }

    public User createStaff(Role role, String username, String password, String firstName, String surname, String address, String phone, String staffNumber) {
        session.requireRole(Role.ADMINISTRATOR);
        Rules.checkNewLogin(users, username, password);
        String id = users.nextUserId();
        String hash = User.hashPassword(password);
        User user = switch (role) {
            case TELLER -> new BankTeller(id, username.trim(), hash, firstName, surname, address, phone, staffNumber);

            case ADMINISTRATOR -> new SystemAdministrator(id, username.trim(), hash, firstName, surname, address, phone, staffNumber);

            default -> throw new BankException("Only staff users can be created here");
        };
        users.insert(user);
        return user;
    }

    public List<ActivityEntry> activity() {
        session.requireRole(Role.ADMINISTRATOR);
        List<ActivityEntry> entries = new ArrayList<>();
        for (Account account : accounts.findAll()) {
            for (Transaction t : account.getTransactions()) {
                entries.add(new ActivityEntry(account.getAccountNumber(), t));
            }
        }
        entries.sort(Comparator.comparing((ActivityEntry e) -> e.transaction().getTimestamp()).reversed());
        return entries;
    }

    private void setActive(String userId, boolean active) {
        User admin = session.requireRole(Role.ADMINISTRATOR);
        if (!active && admin.getUserId().equals(userId)) {
            throw new BankException("You cannot deactivate your own account");
        }
        User user = users.findById(userId);
        if (user == null) {
            throw new BankException("User not found");
        }
        if (active) {
            user.activate();
        } else {
            user.deactivate();
        }
        users.update(user);
    }
}

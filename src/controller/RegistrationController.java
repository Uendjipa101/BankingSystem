package controller;

import dao.AccountDAO;
import dao.UserDAO;
import model.*;

import java.time.LocalDate;

public class RegistrationController {
    private final UserDAO users = new UserDAO();
    private final AccountDAO accounts = new AccountDAO();
    private final Session session;

    public RegistrationController(Session session) {
        this.session = session;
    }

    public IndividualCustomer registerIndividual(String username, String password, String firstName, String surname, String address, String phone, String idNumber) {

        session.requireRole(Role.TELLER);

        Rules.checkNewLogin(users, username, password);

        IndividualCustomer customer = new IndividualCustomer(users.nextUserId(), username.trim(), User.hashPassword(password), firstName, surname, address, phone, idNumber);
        users.insert(customer);
        return customer;
    }

    public CompanyCustomer registerCompany(String username, String password, String contactFirstName, String contactSurname, String address, String phone, String companyName, String registrationNumber) {
        session.requireRole(Role.TELLER);
        Rules.checkNewLogin(users, username, password);
        CompanyCustomer customer = new CompanyCustomer(users.nextUserId(), username.trim(), User.hashPassword(password), contactFirstName, contactSurname, address, phone, companyName, registrationNumber);
        users.insert(customer);
        return customer;
    }

    public Account openSavings(String customerId, String branch) {
        Customer customer = findCustomer(customerId);
        return save(customer, new SavingsAccount(accounts.nextAccountNumber(), branch, LocalDate.now()));
    }

    public Account openInvestment(String customerId, String branch, double initialDeposit) {
        Customer customer = findCustomer(customerId);
        return save(customer, new InvestmentAccount(accounts.nextAccountNumber(), branch, LocalDate.now(), initialDeposit));
    }

    public Account openCheque(String customerId, String branch, String companyName,
                              String companyAddress) {
        Customer customer = findCustomer(customerId);
        EmploymentDetails job = new EmploymentDetails(companyName, companyAddress);
        return save(customer, new ChequeAccount(accounts.nextAccountNumber(), branch, LocalDate.now(), job));
    }

    private Customer findCustomer(String customerId) {
        session.requireRole(Role.TELLER);
        User user = users.findById(customerId);
        if (!(user instanceof Customer customer)) {
            throw new BankException("Customer not found");
        }
        return customer;
    }

    private Account save(Customer customer, Account account) {
        customer.addAccount(account);
        accounts.insert(customer.getUserId(), account);
        return account;
    }
}

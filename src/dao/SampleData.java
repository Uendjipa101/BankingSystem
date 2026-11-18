package dao;

import model.Account;
import model.ChequeAccount;
import model.CompanyCustomer;
import model.Customer;
import model.EmploymentDetails;
import model.IndividualCustomer;
import model.InvestmentAccount;
import model.SavingsAccount;
import model.BankTeller;
import model.SystemAdministrator;
import model.User;

import java.time.LocalDate;

public class SampleData {

    public static void loadIfEmpty() {
        UserDAO users = new UserDAO();
        AccountDAO accounts = new AccountDAO();
        if (users.countUsers() > 0) {
            return;
        }

        String hash = User.hashPassword("password123");
        LocalDate opened = LocalDate.of(2026, 6, 1);

        users.insert(new SystemAdministrator(users.nextUserId(), "admin", hash,
                "Kagiso", "Admin", "Main Mall, Gaborone", "71000001", "A001"));
        users.insert(new BankTeller(users.nextUserId(), "teller", hash,
                "Lerato", "Teller", "Main Mall, Gaborone", "71000002", "T001"));

        String[] first = {"Neo", "Kabo", "Lesedi", "Tebogo", "Mpho",
                "Naledi", "Thabo", "Boitumelo", "Keabetswe", "Onalenna"};
        String[] sur = {"Kgosi", "Molefe", "Sebego", "Moeng", "Dube",
                "Phiri", "Ntsima", "Mosweu", "Radipitse", "Segwai"};

        for (int i = 0; i < 10; i++) {
            String id = users.nextUserId();
            String username = first[i].toLowerCase();
            String address = "Plot " + (10 + i) + ", Gaborone";
            String phone = "7200000" + i;

            Customer customer;
            if (i < 8) {
                customer = new IndividualCustomer(id, username, hash, first[i], sur[i],
                        address, phone, "10000000" + i);
            } else {
                String company = (i == 8) ? "Kalahari Traders" : "Okavango Supplies";
                customer = new CompanyCustomer(id, username, hash, first[i], sur[i],
                        address, phone, company, "BW00000" + i);
            }
            users.insert(customer);

            SavingsAccount savings = new SavingsAccount(accounts.nextAccountNumber(), "Gaborone", opened);
            savings.deposit(1000 + i * 250);
            open(accounts, customer, savings);

            if (i < 5 || i >= 8) {
                InvestmentAccount investment = new InvestmentAccount(accounts.nextAccountNumber(),
                        "Gaborone", opened, 500 + i * 100);
                if (i == 0) {
                    investment.withdraw(100);
                }
                open(accounts, customer, investment);
            }

            if (i < 4) {
                ChequeAccount cheque = new ChequeAccount(accounts.nextAccountNumber(), "Gaborone", opened,
                        new EmploymentDetails("Debswana Mining Company", "Jwaneng"));
                cheque.deposit(5000, "Salary");
                cheque.withdraw(300);
                open(accounts, customer, cheque);
            }
        }
    }

    private static void open(AccountDAO accounts, Customer customer, Account account) {
        customer.addAccount(account);
        accounts.insert(customer.getUserId(), account);
    }
}
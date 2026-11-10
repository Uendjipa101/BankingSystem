package model;

public class CompanyCustomer extends Customer {
    private final String companyName;
    private final String registrationNumber;

    public CompanyCustomer(String userId, String username, String passwordHash, String firstname, String surname, String address, String phoneNumber, String companyName, String registrationNumber){
        super(userId, username, passwordHash, firstname, surname, address, phoneNumber);
        this.companyName = requireText(companyName, "Company name");
        this.registrationNumber = requireText(registrationNumber, "Registration number");
    }

    public String getCompanyName() {
        return companyName;
    }
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    @Override
    protected boolean canHold(Account account) {
        return !(account instanceof ChequeAccount);
    }

    @Override
    public String getDisplayName() {
        return companyName + " (" + registrationNumber + ")";
    }
}

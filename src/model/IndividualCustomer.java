package model;

public class IndividualCustomer extends Customer{
    private final String idNumber;

    public IndividualCustomer(String userId, String username, String passwordHash, String firstname, String surnmae, String address, String phoneNumber,String idNumber) {
        super(userId, username, passwordHash, firstname, surnmae, address, phoneNumber);
        this.idNumber = requireText(idNumber, "ID number");
    }

    public String getIdNumber() {
        return idNumber;
    }

    @Override
    public String getDisplayName() {
        return getFirstname() + " " + getSurname();
    }
}

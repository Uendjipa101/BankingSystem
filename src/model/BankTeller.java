package model;

public class BankTeller extends User{
    private final String staffNumber;

    public BankTeller(String userId, String username, String passwordHash, String firstName, String surname, String address, String phoneNumber, String staffNumber) {
        super(userId, username, passwordHash, Role.TELLER, firstName, surname, address, phoneNumber);
        this.staffNumber = requireText(staffNumber, "Staff number");
    }
    public String getStaffNumber() {
        return staffNumber;
    }

    @Override
    public String getDisplayName() {
        return getFirstname() + " " + getSurname() + " (Staff " + staffNumber + ")";
    }

}

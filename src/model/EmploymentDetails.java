package model;

public class EmploymentDetails {
    private final String companyName;
    private final String companyAddress;

    public EmploymentDetails(String companyName, String companyAddress) {
        if (companyName == null || companyName.isBlank()
                || companyAddress == null || companyAddress.isBlank()) {
    throw new IllegalArgumentException("Company name or address are required");
        }
        this.companyName = companyName;
        this.companyAddress = companyAddress;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }
}

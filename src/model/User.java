package model;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public abstract class User {
    private final String userId;
    private final String username;
    private final String passwordHash;
    private final Role role;
    private boolean active;
    private final String firstname;
    private final String surname;
    private final String address;
    private final String phoneNumber;

    public User(String userId, String username, String passwordHash, Role role,
                String firstname, String surname, String address, String phoneNumber) {
        this.userId = requireText(userId, "User id");
        this.username = requireText(username, "Username");
        this.passwordHash = requireText(passwordHash, "Password");
        if (role == null){
            throw new IllegalArgumentException("Role is required");
        }
        this.role = role;
        this.firstname = requireText(firstname, "First name");
        this.surname = requireText(surname, "Surname");
        this.address = requireText(address, "Address");
        this.phoneNumber = requireText(phoneNumber, "Phone number");
        this.active = true;
    }

    protected static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e){
            throw new IllegalArgumentException("SHA-256 is not available", e);
        }
    }

    public boolean authenticate(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return passwordHash.equals(hashPassword(password));
    }

    public void activate() {
        active = true;
    }
    public void deactivate() {
        active = false;
    }
    public boolean isActive() {
        return active;
    }
    public abstract String getDisplayName();

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getSurname() {
        return surname;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}

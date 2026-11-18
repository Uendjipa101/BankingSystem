package controller;

import dao.UserDAO;
import model.User;

public class LoginController {
    private final UserDAO users = new UserDAO();
    private final Session session;

    public LoginController(Session session){
        this.session = session;
    }

    public User login(String username, String password){
        if (username == null || username.isBlank() || password == null || password.isEmpty()){
            throw new BankException("Enter your username and password");
        }
        User user = users.findByUsername(username.trim());
        if (user == null){
            throw new BankException("Invalid username or password");
        }
        if (!user.isActive()){
            throw new BankException("This account is deactivated. Contact the administrator for help");
        }
        if (!user.authenticate(password)){
            throw new BankException("Invalid username or password");
        }

        session.start(user);
        return user;
    }
    public void logout(){
        session.end();
    }
}

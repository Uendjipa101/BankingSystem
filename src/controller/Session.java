package controller;

import model.Role;
import model.User;

public class Session {
    private User currentUser;

    public void start(User user){
        this.currentUser = user;
    }
    public void end(){
        this.currentUser = null;
    }
    public User getCurrentUser(){
        return currentUser;
    }
    public boolean isLoggedIn(){
        return currentUser != null;
    }

    public User requireRole(Role role){
        if (currentUser == null){
            throw new BankException("Please log in first");
        }
        if (currentUser.getRole() != role){
            throw new BankException("You are not allowed to perform this action");
        }
        return currentUser;
    }
}

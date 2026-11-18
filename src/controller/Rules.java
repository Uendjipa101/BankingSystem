package controller;

import dao.UserDAO;

final class Rules {
    private Rules() { }

    static void checkNewLogin(UserDAO users, String username, String password) {
        if (username == null || username.isBlank()) {
            throw new BankException("Username is required");
        }
        if (password == null || password.length() < 6) {
            throw new BankException("Password must be at least 6 characters");
        }
        if(users.findByUsername(username.trim()) != null){
            throw new BankException("That username is already taken");
        }
    }
}


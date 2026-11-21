package view;

import controller.InterestController;
import controller.LoginController;
import controller.Session;
import dao.DatabaseConnection;
import dao.SampleData;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;

public class BankApp extends Application {
    private final Session session = new Session();
    private LoginController loginController;
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        try {
            DatabaseConnection.initialise();
            SampleData.loadIfEmpty();
            new InterestController().payDueInterest();
        } catch (RuntimeException e) {
            Ui.error("Could not start the system: " + e.getMessage());
            Platform.exit();
            return;
        }
        loginController = new LoginController(session);
        stage.setTitle("Banking System");
        showLogin();
        stage.show();
    }

    public void showLogin() {
        show(new LoginView(this, loginController).build(), 640, 460);
    }

    public void showHome(User user) {
        switch (user.getRole()) {
            case CUSTOMER -> show(new CustomerView(this, session).build(), 960, 680);
            case TELLER -> show(new TellerView(this, session).build(), 900, 640);
            case ADMINISTRATOR -> show(new AdminView(this, session).build(), 960, 640);
        }
    }

    public void logout() {
        loginController.logout();
        showLogin();
    }

    private void show(Parent root, double width, double height) {
        stage.setScene(new Scene(root, width, height));
        stage.centerOnScreen();
    }
}

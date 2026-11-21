package view;

import controller.LoginController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.User;

public class LoginView {
    private final BankApp app;
    private final LoginController controller;

    public LoginView(BankApp app, LoginController controller){
        this.app = app;
        this.controller = controller;
    }

    public Parent build(){
        Label title = Ui.title("Banking System");
        Label subtitle = new Label("Please log in to continue");

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Button login = Ui.button("Login");
        login.setDefaultButton(true);
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(e -> Ui.run(() ->{
            User user = controller.login(username.getText(), password.getText());
            password.clear();
            app.showHome(user);
        }));
        Label hint = new Label("Demo logins: admin, teller, neo   (password: password123)");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #777777;");

        VBox card = new VBox(12, title, subtitle, username, password, login, hint);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(360);
        card.setMaxHeight(VBox.USE_PREF_SIZE);
        card.setStyle("-fx-background-color: white; -fx-border-color: #C9D6E2; -fx-border-radius: 6; "
                + "-fx-background-radius: 6;");

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: #EEF3F8;");
        return root;
    }
}

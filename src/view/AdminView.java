package view;

import controller.AdminController;
import controller.BankException;
import controller.Session;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Role;
import model.User;

public class AdminView {
    private final BankApp app;
    private final Session session;
    private final AdminController admin;

    private final TableView<User> userTable = new TableView<>();
    private final TableView<AdminController.ActivityEntry> activityTable = new TableView<>();

    public AdminView(BankApp app, Session session) {
        this.app = app;
        this.session = session;
        this.admin = new AdminController(session);
    }

    public Parent build() {
        BorderPane root = new BorderPane();
        root.setTop(Ui.header("System Administrator", session.getCurrentUser().getDisplayName(), app::logout));

        Tab users = new Tab("Users and access", usersPane());
        Tab create = new Tab("Create staff user", createPane());
        Tab activity = new Tab("System activity", activityPane());
        for (Tab tab : new Tab[]{users, create, activity}) {
            tab.setClosable(false);
        }
        users.setOnSelectionChanged(e -> reloadUsers());
        activity.setOnSelectionChanged(e -> reloadActivity());

        root.setCenter(new TabPane(users, create, activity));
        reloadUsers();
        return root;
    }

    private Parent usersPane() {
        userTable.getColumns().add(Ui.<User>column("ID", 70, User::getUserId));
        userTable.getColumns().add(Ui.<User>column("Username", 120, User::getUsername));
        userTable.getColumns().add(Ui.<User>column("Name", 260, User::getDisplayName));
        userTable.getColumns().add(Ui.<User>column("Role", 130, u -> u.getRole().name()));
        userTable.getColumns().add(Ui.<User>column("Status", 110, u -> u.isActive() ? "Active" : "Deactivated"));
        VBox.setVgrow(userTable, Priority.ALWAYS);

        Button activate = Ui.button("Activate");
        activate.setOnAction(e -> Ui.run(() -> {
            admin.activateUser(selectedUser().getUserId());
            reloadUsers();
        }));
        Button deactivate = Ui.button("Deactivate");
        deactivate.setOnAction(e -> Ui.run(() -> {
            admin.deactivateUser(selectedUser().getUserId());
            reloadUsers();
        }));
        Button refresh = Ui.button("Refresh");
        refresh.setOnAction(e -> reloadUsers());

        VBox box = new VBox(10, userTable, new HBox(10, activate, deactivate, refresh));
        box.setPadding(new Insets(14));
        return box;
    }

    private Parent createPane() {
        ChoiceBox<Role> role = new ChoiceBox<>(FXCollections.observableArrayList(Role.TELLER, Role.ADMINISTRATOR));
        role.setValue(Role.TELLER);
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        TextField first = new TextField();
        TextField surname = new TextField();
        TextField address = new TextField();
        TextField phone = new TextField();
        TextField staffNumber = new TextField();

        GridPane form = Ui.form();
        Ui.addRow(form, 0, "Role", role);
        Ui.addRow(form, 1, "Username", username);
        Ui.addRow(form, 2, "Password (min 6)", password);
        Ui.addRow(form, 3, "First name", first);
        Ui.addRow(form, 4, "Surname", surname);
        Ui.addRow(form, 5, "Address", address);
        Ui.addRow(form, 6, "Phone number", phone);
        Ui.addRow(form, 7, "Staff number", staffNumber);

        Button create = Ui.button("Create user");
        create.setOnAction(e -> Ui.run(() -> {
            User user = admin.createStaff(role.getValue(), username.getText(), password.getText(),
                    first.getText(), surname.getText(), address.getText(), phone.getText(),
                    staffNumber.getText());
            Ui.info("Created " + user.getRole() + " " + user.getDisplayName() + " (id " + user.getUserId() + ").");
            for (TextField f : new TextField[]{username, first, surname, address, phone, staffNumber}) {
                f.clear();
            }
            password.clear();
            reloadUsers();
        }));

        VBox box = new VBox(10, form, create);
        box.setPadding(new Insets(10));
        return box;
    }

    private Parent activityPane() {
        activityTable.getColumns().add(Ui.<AdminController.ActivityEntry>column("Date", 150,
                e -> e.transaction().getTimestamp().format(Ui.TIME)));
        activityTable.getColumns().add(Ui.<AdminController.ActivityEntry>column("Account", 100,
                AdminController.ActivityEntry::accountNumber));
        activityTable.getColumns().add(Ui.<AdminController.ActivityEntry>column("Type", 110,
                e -> e.transaction().getType().name()));
        activityTable.getColumns().add(Ui.<AdminController.ActivityEntry>column("Amount", 110,
                e -> String.format("%.2f", e.transaction().getAmount())));
        activityTable.getColumns().add(Ui.<AdminController.ActivityEntry>column("Balance after", 130,
                e -> String.format("%.2f", e.transaction().getBalanceAfter())));
        activityTable.getColumns().add(Ui.<AdminController.ActivityEntry>column("Description", 220,
                e -> e.transaction().getDescription()));
        VBox.setVgrow(activityTable, Priority.ALWAYS);

        Button refresh = Ui.button("Refresh");
        refresh.setOnAction(e -> reloadActivity());
        VBox box = new VBox(10, activityTable, refresh);
        box.setPadding(new Insets(14));
        return box;
    }

    private User selectedUser() {
        User user = userTable.getSelectionModel().getSelectedItem();
        if (user == null) {
            throw new BankException("Select a user first.");
        }
        return user;
    }

    private void reloadUsers() {
        Ui.run(() -> userTable.setItems(FXCollections.observableArrayList(admin.listUsers())));
    }

    private void reloadActivity() {
        Ui.run(() -> activityTable.setItems(FXCollections.observableArrayList(admin.activity())));
    }
}
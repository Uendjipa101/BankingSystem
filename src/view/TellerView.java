package view;

import controller.RegistrationController;
import controller.Session;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.Account;
import model.Customer;
import model.User;

public class TellerView {
    private final BankApp app;
    private final Session session;
    private final RegistrationController registration;

    private final ComboBox<User> customerBox = new ComboBox<>();

    public TellerView(BankApp app, Session session) {
        this.app = app;
        this.session = session;
        this.registration = new RegistrationController(session);
    }

    public Parent build() {
        BorderPane root = new BorderPane();
        root.setTop(Ui.header("Bank Teller", session.getCurrentUser().getDisplayName(), app::logout));

        Tab registerTab = new Tab("Register customer", registerForm());
        Tab openTab = new Tab("Open account", openAccountForm());
        registerTab.setClosable(false);
        openTab.setClosable(false);
        openTab.setOnSelectionChanged(e -> reloadCustomers());

        root.setCenter(new TabPane(registerTab, openTab));
        reloadCustomers();
        return root;
    }

    private Parent registerForm() {
        ChoiceBox<String> kind = new ChoiceBox<>(FXCollections.observableArrayList("Individual", "Company"));
        kind.setValue("Individual");
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        TextField first = new TextField();
        TextField surname = new TextField();
        TextField address = new TextField();
        TextField phone = new TextField();
        TextField idNumber = new TextField();
        TextField companyName = new TextField();
        TextField registrationNumber = new TextField();

        GridPane common = Ui.form();
        Ui.addRow(common, 0, "Customer type", kind);
        Ui.addRow(common, 1, "Username", username);
        Ui.addRow(common, 2, "Password (min 6)", password);
        Ui.addRow(common, 3, "First name (contact person for a company)", first);
        Ui.addRow(common, 4, "Surname", surname);
        Ui.addRow(common, 5, "Address", address);
        Ui.addRow(common, 6, "Phone number", phone);

        GridPane individualPart = Ui.form();
        Ui.addRow(individualPart, 0, "ID number", idNumber);
        GridPane companyPart = Ui.form();
        Ui.addRow(companyPart, 0, "Company name", companyName);
        Ui.addRow(companyPart, 1, "Registration number", registrationNumber);
        companyPart.setVisible(false);
        companyPart.setManaged(false);

        kind.valueProperty().addListener((obs, old, now) -> {
            boolean company = "Company".equals(now);
            companyPart.setVisible(company);
            companyPart.setManaged(company);
            individualPart.setVisible(!company);
            individualPart.setManaged(!company);
        });

        Button register = Ui.button("Register customer");
        register.setOnAction(e -> Ui.run(() -> {
            Customer customer;
            if ("Company".equals(kind.getValue())) {
                customer = registration.registerCompany(username.getText(), password.getText(),
                        first.getText(), surname.getText(), address.getText(), phone.getText(),
                        companyName.getText(), registrationNumber.getText());
            } else {
                customer = registration.registerIndividual(username.getText(), password.getText(),
                        first.getText(), surname.getText(), address.getText(), phone.getText(),
                        idNumber.getText());
            }
            Ui.info("Registered " + customer.getDisplayName() + " (id " + customer.getUserId()
                    + "). You can now open an account for them.");
            for (TextField f : new TextField[]{username, first, surname, address, phone,
                    idNumber, companyName, registrationNumber}) {
                f.clear();
            }
            password.clear();
            reloadCustomers();
        }));

        VBox box = new VBox(5, common, individualPart, companyPart, register);
        box.setPadding(new javafx.geometry.Insets(10));
        return box;
    }

    private Parent openAccountForm() {
        customerBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user == null ? "" : user.getUserId() + " - " + user.getDisplayName();
            }

            @Override
            public User fromString(String text) {
                return null;
            }
        });
        customerBox.setPrefWidth(320);

        ChoiceBox<String> type = new ChoiceBox<>(FXCollections.observableArrayList("Savings", "Investment", "Cheque"));
        type.setValue("Savings");
        TextField branch = new TextField("Gaborone");
        TextField initialDeposit = new TextField();
        initialDeposit.setPromptText("At least 500.00");
        TextField employer = new TextField();
        TextField employerAddress = new TextField();

        GridPane common = Ui.form();
        Ui.addRow(common, 0, "Customer", customerBox);
        Ui.addRow(common, 1, "Account type", type);
        Ui.addRow(common, 2, "Branch", branch);

        GridPane investmentPart = Ui.form();
        Ui.addRow(investmentPart, 0, "Opening deposit (BWP)", initialDeposit);
        GridPane chequePart = Ui.form();
        Ui.addRow(chequePart, 0, "Employer company name", employer);
        Ui.addRow(chequePart, 1, "Employer company address", employerAddress);
        for (GridPane part : new GridPane[]{investmentPart, chequePart}) {
            part.setVisible(false);
            part.setManaged(false);
        }

        type.valueProperty().addListener((obs, old, now) -> {
            investmentPart.setVisible("Investment".equals(now));
            investmentPart.setManaged("Investment".equals(now));
            chequePart.setVisible("Cheque".equals(now));
            chequePart.setManaged("Cheque".equals(now));
        });

        Button open = Ui.button("Open account");
        open.setOnAction(e -> Ui.run(() -> {
            User customer = customerBox.getValue();
            if (customer == null) {
                throw new IllegalArgumentException("Select a customer first.");
            }
            String id = customer.getUserId();
            Account account;
            switch (type.getValue()) {
                case "Investment" -> account = registration.openInvestment(id, branch.getText(),
                        Ui.parseAmount(initialDeposit.getText()));
                case "Cheque" -> account = registration.openCheque(id, branch.getText(),
                        employer.getText(), employerAddress.getText());
                default -> account = registration.openSavings(id, branch.getText());
            }
            Ui.info("Account opened: " + account);
            initialDeposit.clear();
            employer.clear();
            employerAddress.clear();
        }));

        VBox box = new VBox(5, common, investmentPart, chequePart, open);
        box.setPadding(new javafx.geometry.Insets(10));
        return box;
    }

    private void reloadCustomers() {
        Ui.run(() -> customerBox.setItems(FXCollections.observableArrayList(registration.listCustomers())));
    }
}
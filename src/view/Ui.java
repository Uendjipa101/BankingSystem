package view;

import controller.ReceiptController;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public final class Ui {
    public static final String NAVY = "#1F4E79";
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Ui (){

    }
    // Runs an action and shows any error message in a dialog
    public static void run(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            error(e.getMessage() == null ? "Something went wrong." : e.getMessage());
        }
    }

    public static void error(String message) {
        alert(Alert.AlertType.ERROR, "Error", message);
    }

    public static void info(String message) {
        alert(Alert.AlertType.INFORMATION, "Banking System", message);
    }

    private static void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static String money(double value) {
        return String.format("BWP %.2f", value);
    }

    public static double parseAmount(String text) {
        try {
            return Double.parseDouble(text.trim().replace(",", "."));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Enter a valid amount, for example 250.00");
        }
    }

    public static Button button(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + NAVY + "; -fx-text-fill: white; " + "-fx-font-weight: bold; -fx-padding: 7 16 7 16; -fx-background-radius: 4;");
        return b;
    }

    public static Label title(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + NAVY + ";");
        return l;
    }

    public static Label section(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        return l;
    }

    public static HBox header(String heading, String subtitle, Runnable onLogout) {
        Label h = new Label(heading);
        h.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill: #D6E4F0;");
        javafx.scene.layout.VBox texts = new javafx.scene.layout.VBox(2, h, s);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logout = new Button("Log out");
        logout.setOnAction(e -> onLogout.run());
        HBox bar = new HBox(10, texts, spacer, logout);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 18, 12, 18));
        bar.setStyle("-fx-background-color: " + NAVY + ";");
        return bar;
    }

    public static <T> TableColumn<T, String> column(String title, double width, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new SimpleStringProperty(value.apply(cell.getValue())));
        return column;
    }

    public static GridPane form() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        return grid;
    }

    public static void addRow(GridPane grid, int row, String label, Node field) {
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
    }

    // Shows receipt or statement text in a dialog, with an option to save it as a text file
    public static void showText(ReceiptController receipts, String heading, String text, String fileName) {
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setFont(Font.font("Monospaced", 13));
        area.setPrefSize(470, 330);
        ButtonType save = new ButtonType("Save to file", ButtonBar.ButtonData.LEFT);
        Alert alert = new Alert(Alert.AlertType.NONE, "", save, ButtonType.CLOSE);
        alert.setTitle(heading);
        alert.getDialogPane().setContent(area);
        alert.showAndWait().ifPresent(choice -> {
            if (choice == save) {
                run(() -> info("Saved to " + receipts.saveToFile(text, fileName).toAbsolutePath()));
            }
        });
    }
}


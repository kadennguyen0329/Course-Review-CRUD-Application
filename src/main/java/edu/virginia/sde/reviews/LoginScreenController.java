package edu.virginia.sde.reviews;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import java.sql.SQLException;
import java.util.Optional;
import javafx.stage.Stage;
import java.io.IOException;


public class LoginScreenController {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private TextField newUsername;
    @FXML private PasswordField newPassword;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button exitButton;
    @FXML private Label errorMessage;
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    public void handleLoginButtonAction(ActionEvent event) {
        if (username.getText().isEmpty() && password.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please enter your username and password", "User" +
                    "name and password are required");
            return;
        }
        try {
            CourseReviewDatabase database = CourseReviewDatabase.getInstance();
            if (!database.userNameExists(username.getText())) {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "User not found for inputted Username");
            }
            else {
                Optional<User> authenticatedUser = database.getUser(username.getText(), password.getText());
                if (authenticatedUser.isPresent()) {
                    UserService.setCurrentUser(authenticatedUser.get());
                    username.clear();
                    password.clear();
                    switchToCourseSearchScreen(event);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Password");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        if (newUsername.getText().isEmpty() && newPassword.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please enter your username and password", "New" +
                    " account requires username and password");
        }
        else if (newPassword.getText().length() < 8) {
            showAlert(Alert.AlertType.ERROR, "Invalid password length", "Password must be at least 8 characters long");
            return;
        }
        try
        {
            CourseReviewDatabase database = CourseReviewDatabase.getInstance();
            if (database.userNameExists(newUsername.getText())) {
                showAlert(Alert.AlertType.ERROR, "New Account Failed", "Username already exists");
            }
            else {
                User newUser = new User(0, newUsername.getText(), newPassword.getText());
                database.createUser(newUser);
                showAlert(Alert.AlertType.INFORMATION, "Account Creation Successful!", "Account created successfully! Please log in with your new account.");
                newUsername.clear();
                newPassword.clear();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void switchToCourseSearchScreen (ActionEvent event) {
        //Source: https://www.youtube.com/watch?v=hcM-R-YOKkQ&t=251s
        //Helped me figure out how to switch scenes
        try {
            root = FXMLLoader.load(getClass().getResource("course-search.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Course Search");
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid site URL", "Unable to load the Course Search screen.");
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleExit(ActionEvent event) {
        // Exit the application
        javafx.application.Platform.exit();
    }
}

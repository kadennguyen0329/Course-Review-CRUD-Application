package edu.virginia.sde.reviews;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;

public class CourseSearchScreenController {
    @FXML
    private TableView<Course> courseTable;
    @FXML
    private TableColumn<Course, String> subjectMnemonicColumn;
    @FXML
    private TableColumn<Course, Integer> courseNumberColumn;
    @FXML
    private TableColumn<Course, String> titleColumn;
    @FXML
    private TableColumn<Course, Double> averageRatingColumn;

    @FXML
    private TextField searchMnemonicField;
    @FXML
    private TextField searchNumberField;
    @FXML
    private TextField searchTitleField;

    @FXML
    private TextField newMnemonicField;
    @FXML
    private TextField newNumberField;
    @FXML
    private TextField newTitleField;

    @FXML
    private Button myReviewsButton;
    @FXML
    private Button courseSearchButton;
    @FXML
    private Button addCourseButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button viewCoursesButton;

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    public void initialize() {
        subjectMnemonicColumn.setCellValueFactory(new PropertyValueFactory<>("subjectMnemonic"));
        courseNumberColumn.setCellValueFactory(new PropertyValueFactory<>("courseNumber"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("courseTitle"));
        averageRatingColumn.setCellValueFactory(new PropertyValueFactory<>("averageRating"));
        averageRatingColumn.setCellFactory(column -> new TableCell<Course, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText(null); // Display blank cell for 0 or null values
                } else {
                    setText(String.format("%.2f", item)); // Format non-zero values to 2 decimal places
                }
            }
        });
        updateCourseTable();
    }
    @FXML
    private void searchCoursesPressed(ActionEvent event) {
        String mnemonic = searchMnemonicField.getText();
        String courseNumberStr = searchNumberField.getText();
        String title = searchTitleField.getText();

        if (!CourseValidator.isValidMnemonic(mnemonic) && !mnemonic.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Subject mnemonic must be 2-4 letters.");
        }
        if (!CourseValidator.isValidCourseNumber(courseNumberStr) && !courseNumberStr.isEmpty())
        {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Course number must be exactly 4 digits.");
        }
        if (!CourseValidator.isValidCourseTitle(title) && !title.isEmpty())
        {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Course title must be between 1 and 50 characters.");
        }

        int courseNumber = 0;
        if (!courseNumberStr.isEmpty()) {
            courseNumber = Integer.parseInt(courseNumberStr);
        }
        try {
            CourseReviewDatabase database = CourseReviewDatabase.getInstance();
            List<Course> courses = database.searchCourses(mnemonic, courseNumber, title);
            if (courses.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "No Match", "Course not found.");
            }
            courseTable.setItems(FXCollections.observableArrayList(courses));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    private void addCoursePressed(ActionEvent event) {
        String mnemonic = newMnemonicField.getText();
        String courseNumString = newNumberField.getText();
        String title = newTitleField.getText();
        if (mnemonic.isEmpty() || courseNumString.isEmpty() || title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Fields Missing", "To add course, all fields must be filled out");
        }
        else {
            try {
                CourseReviewDatabase database = CourseReviewDatabase.getInstance();
                int courseNumber = Integer.parseInt(courseNumString);
                if (!database.validateCourses(mnemonic, courseNumber, title)) {
                    showAlert(Alert.AlertType.ERROR, "Duplicate Found", "This course already exists");
                } else {
                    Course newCourse = new Course(0, mnemonic, courseNumber, title, 0.0);//replace averageRating with actual value, placeholder for now
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course added Successfully");
                    database.addCourse(newCourse);
                    updateCourseTable();
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Course number must be a valid 4 digit integer.");
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", e.getMessage());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    public void handleMyReviews(ActionEvent event){
        try {
            root = FXMLLoader.load(getClass().getResource("my-reviews.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("My Reviews");
            stage.show();

        } catch (IOException e) {
            System.out.println("Error going back");
        }
    }
    @FXML
    private void viewCoursesPressed(ActionEvent event) {
        Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            String mnemonic = selectedCourse.getSubjectMnemonic();
            int courseNumber = selectedCourse.getCourseNumber();
            String title = selectedCourse.getCourseTitle();
            try
            {
                CourseReviewDatabase database = CourseReviewDatabase.getInstance();
                Course viewCourse = database.getTableSelectedCourse(mnemonic, courseNumber, title);
                CourseService.setCurrentCourse(viewCourse);

                switchToCourseReview(event);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }
    @FXML
    private void switchToLoginScreen (ActionEvent event) {
        try {
            root = FXMLLoader.load(getClass().getResource("login.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load Course Search screen.");
        }
    }

    private void switchToCourseReview (ActionEvent event) {
        try {
            root = FXMLLoader.load(getClass().getResource("course-review.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Course Review");
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load Course Search screen.");
        }
    }

    private void updateCourseTable() {
        try {
            CourseReviewDatabase database = CourseReviewDatabase.getInstance();

            //This query gets every course
            List<Course> courses = database.searchCourses("", 0, "");

            courseTable.setItems(FXCollections.observableArrayList(courses));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update courses.");
        }
    }
        private void showAlert (Alert.AlertType alertType, String title, String message){
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

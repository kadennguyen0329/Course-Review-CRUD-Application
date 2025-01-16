package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class MyReviewsController {

    // FXML annotated variables for fx:id elements
    @FXML
    private Button viewCourse;
    @FXML
    private Button backToSearch;
    @FXML
    private TableView<ExtendedReview> tableView;
    @FXML
    private TableColumn<ExtendedReview, String> mnemonicColumn;
    @FXML
    private TableColumn<ExtendedReview, Integer> numberColumn;
    @FXML
    private TableColumn<ExtendedReview, String> commentColumn;
    @FXML
    private TableColumn<ExtendedReview, Double> ratingColumn;

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    public void initialize() {
        // Set up columns in the table
        mnemonicColumn.setCellValueFactory(new PropertyValueFactory<>("subjectMnemonic"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("courseNumber"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        try {
            // Get data from database and add to table view
            CourseReviewDatabase database = CourseReviewDatabase.getInstance();
            List<ExtendedReview> reviews = database.getExtendedReviewsByUserID(UserService.getCurrentUser().getId()); // Replace 1 with actual user ID
            ObservableList<ExtendedReview> reviewList = FXCollections.observableArrayList(reviews);
            tableView.setItems(reviewList);
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception appropriately (e.g., show an error message)
        }
    }

    // Method stubs for onAction events
    @FXML
    private void handleViewCourse(ActionEvent event) {
        ExtendedReview selectedReview = tableView.getSelectionModel().getSelectedItem();
        if (selectedReview != null) {
            int courseID = selectedReview.getCourseId();
            try
            {
                CourseReviewDatabase database = CourseReviewDatabase.getInstance();
                Course viewCourse = database.getCourseByID(courseID).get();
                CourseService.setCurrentCourse(viewCourse);

                switchToCourseReview(event);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @FXML
    public void handleBackToSearch(ActionEvent event){
        try {
            root = FXMLLoader.load(getClass().getResource("course-search.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setHeight(720.0);
            stage.setWidth(1280.0);
            scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("My Reviews");
            stage.show();

        } catch (IOException e) {
            System.out.println("Error going back");
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
            e.printStackTrace();
        }
    }

}

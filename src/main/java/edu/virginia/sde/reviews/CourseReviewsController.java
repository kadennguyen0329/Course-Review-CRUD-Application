package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class CourseReviewsController {
    @FXML private Label courseName;
    @FXML private Label averageRating;
    @FXML private TableView<Review> reviewsTable;
    @FXML private TableColumn<Review, Double> ratingColumn;
    @FXML private TableColumn<Review, String> commentColumn;
    @FXML private TableColumn<Review, Timestamp> timestampColumn;
    @FXML private Button backToSearch;
    @FXML private TextField inputRating;
    @FXML private TextArea inputComment;
    @FXML private Button createOrUpdate;
    @FXML private Button delete;
    @FXML private Label error;

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    public void initialize() {
        courseName.setText(CourseService.getCurrentCourse().getSubjectMnemonic() + " " +
                        CourseService.getCurrentCourse().getCourseNumber()+" "+
                CourseService.getCurrentCourse().getCourseTitle());
        averageRating.setText(""+CourseService.getCurrentCourse().getAverageRating() + "/5");
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        reviewsTable.setRowFactory(tv -> new TableRow<Review>() {
            @Override
            protected void updateItem(Review item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle(""); // Reset style for empty rows
                } else {
                    int currentUserId = UserService.getCurrentUser().getId();
                    if (item.getUserId() == currentUserId) {
                        setStyle("-fx-background-color: lightblue;"); // Highlight color
                    } else {
                        setStyle(""); // Default style
                    }
                }
            }
        });

        loadReviews();
    }

    @FXML
    public void handleBackToSearch(ActionEvent event){
        try {
            root = FXMLLoader.load(getClass().getResource("course-search.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Course Search");
            stage.show();

        } catch (IOException e) {
            System.out.println("Error going back");
        }
    }

    public void handleCreateOrUpdate() {
        String ratingText = inputRating.getText().trim();
        String comment = inputComment.getText();

        // Validate that the rating is a non-empty integer between 1 and 5
        try {
            int rating = Integer.parseInt(ratingText);
            if (rating < 1 || rating > 5) {
                error.setText("Rating must be an integer between 1 and 5.");
                return;
            }

            // If validation passes, clear the error message
            error.setText("");

            int courseId = CourseService.getCurrentCourse().getCourseID();
            int userId = UserService.getCurrentUser().getId();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            Review newReview = new Review(0, courseId, userId, rating, comment, timestamp);

            try {
                CourseReviewDatabase database = CourseReviewDatabase.getInstance();
                database.addOrUpdateReview(newReview);
                database.updateAverageRating(CourseService.getCurrentCourse().getCourseID());
                loadReviews(); // Refresh the TableView
            } catch (SQLException e) {
                e.printStackTrace();
                error.setText("Error saving review.");
            }
        } catch (NumberFormatException e) {
            error.setText("Rating must be a non-empty integer.");
        }
    }

    public void handleDelete(){
        int userId = UserService.getCurrentUser().getId();
        int courseId = CourseService.getCurrentCourse().getCourseID();

        try {
            CourseReviewDatabase database = CourseReviewDatabase.getInstance();

            // Delete the review for the current user and course
            database.deleteReviewByUserAndCourse(userId, courseId);

            // Refresh the TableView by reloading reviews
            loadReviews();

        } catch (SQLException e) {
            e.printStackTrace();
            error.setText("Error deleting review.");
        }
    }

    private void loadReviews() {
        try {
            CourseReviewDatabase database = CourseReviewDatabase.getInstance();
            int currentCourseId = CourseService.getCurrentCourse().getCourseID();
            List<Review> reviews = database.getReviewsByCourseId(currentCourseId);
            ObservableList<Review> reviewsList = FXCollections.observableArrayList(reviews);
            reviewsTable.setItems(reviewsList);
            double average = database.updateAverageRating(CourseService.getCurrentCourse().getCourseID());
            if(average == 0.0)
                averageRating.setText("None");
            else
                averageRating.setText(""+String.format("%.2f", average) + "/5");
        } catch (SQLException e) {
            e.printStackTrace();
            error.setText("Error loading reviews.");
        }
    }
}

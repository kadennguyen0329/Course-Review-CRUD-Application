package edu.virginia.sde.reviews;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourseReviewDatabase {
    private static final String DatabaseConnection = "jdbc:sqlite:courseReview.sqlite";
    private final Connection connection;
    private static CourseReviewDatabase instance;

    private CourseReviewDatabase() throws SQLException {
        connection = DriverManager.getConnection(DatabaseConnection);
        connection.setAutoCommit(false);
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        createTablesIfNotExists();

    }

    private void createTablesIfNotExists() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String createUsersTable = "CREATE TABLE IF NOT EXISTS Users (" +
                    "UserID INTEGER PRIMARY KEY, " +
                    "Username TEXT NOT NULL, " +
                    "Password TEXT NOT NULL" +
                    ");";
            String createCoursesTable = "CREATE TABLE IF NOT EXISTS Courses (" +
                    "CourseID INTEGER PRIMARY KEY, " +
                    "SubjectMnemonic TEXT NOT NULL, " +
                    "CourseNumber INTEGER NOT NULL, " +
                    "courseTitle TEXT NOT NULL, " +
                    "averageRating REAL NOT NULL " +
                    ");";
            String createReviewsTable = "CREATE TABLE IF NOT EXISTS Reviews (" +
                    "ReviewID INTEGER PRIMARY KEY, " +
                    "CourseID INTEGER NOT NULL, " +
                    "UserID INTEGER NOT NULL, " +
                    "Rating INTEGER NOT NULL CHECK(Rating BETWEEN 1 AND 5), " +
                    "Comment TEXT, " +
                    "Timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (CourseID) REFERENCES Courses(CourseID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE" +
                    ");";
            statement.execute(createUsersTable);
            statement.execute(createCoursesTable);
            statement.execute(createReviewsTable);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public static CourseReviewDatabase getInstance() throws SQLException {
        if (instance == null) {
            instance = new CourseReviewDatabase();
        }
        return instance;
    }

    public void commit() throws SQLException {
        connection.commit();
    }

    public void rollback() throws SQLException {
        connection.rollback();
    }

    public void createUser(User user) throws SQLException {
        String addUser = "INSERT INTO Users (UserID, Username, Password) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(addUser)) {
            int uniqueID = generateUniqueUserID();
            statement.setInt(1, uniqueID);
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPassword());
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public boolean userNameExists(String username) throws SQLException {
        String checkUsername = "SELECT * FROM Users WHERE Username = ?";
        try (PreparedStatement statement = connection.prepareStatement(checkUsername)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public Optional<User> getUser(String username, String password) throws SQLException {
        String getUser = "SELECT UserID, Username, Password FROM Users WHERE Username = ? AND Password = ?";
        try (PreparedStatement statement = connection.prepareStatement(getUser)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new User(resultSet.getInt("UserID"), resultSet.getString("Username"),
                            resultSet.getString("Password")));
                }
            }
        }
        return Optional.empty();
    }

    public void addCourse(Course course) throws SQLException {
        String addCourse = "INSERT INTO Courses (CourseID, SubjectMnemonic, CourseNumber, CourseTitle, AverageRating) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(addCourse)) {
            int uniqueID = generateUniqueCourseID();
            statement.setInt(1, uniqueID);
            statement.setString(2, course.getSubjectMnemonic());
            statement.setInt(3, course.getCourseNumber());
            statement.setString(4, course.getCourseTitle());
            statement.setDouble(5, course.getAverageRating());
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public List<ExtendedReview> getExtendedReviewsByUserID(int userID) {
        List<ExtendedReview> extendedReviews = new ArrayList<>();

        try {
            // Retrieve reviews for the user
            List<Review> reviews = getReviewsByUserId(userID);

            for (Review review : reviews) {
                // Retrieve course information using course ID from the review
                Course course = getCourseByID(review.getCourseId()).get();

                // Create an ExtendedReview object with additional details
                ExtendedReview extendedReview = new ExtendedReview(
                        review.getId(),
                        review.getCourseId(),
                        review.getUserId(),
                        review.getRating(),
                        review.getComment(),
                        review.getTimestamp(),
                        course.getSubjectMnemonic(),
                        course.getCourseNumber()
                );
                extendedReviews.add(extendedReview);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return extendedReviews;
    }

    public List<Review> getReviewsByUserId(int userId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String query = "SELECT * FROM Reviews WHERE UserID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("ReviewID");
                    int courseId = resultSet.getInt("CourseID");
                    int rating = resultSet.getInt("Rating");
                    String comment = resultSet.getString("Comment");
                    Timestamp timestamp = resultSet.getTimestamp("Timestamp");
                    reviews.add(new Review(id, courseId, userId, rating, comment, timestamp));
                }
            }
        }
        catch (SQLException e) {
            throw new SQLException("Error retrieving course by ID", e);
        }
        return reviews;
    }

    public Optional<Course> getCourseByID(int courseID) throws SQLException {
        String query = "SELECT * FROM Courses WHERE CourseID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, courseID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String subjectMnemonic = resultSet.getString("SubjectMnemonic");
                    int courseNumber = resultSet.getInt("CourseNumber");
                    String courseTitle = resultSet.getString("CourseTitle");
                    double averageRating = resultSet.getDouble("AverageRating");

                    Course course = new Course(courseID, subjectMnemonic, courseNumber, courseTitle, averageRating);
                    return Optional.of(course);
                } else {
                    return Optional.empty(); // Return empty if no course is found
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error retrieving course by ID", e);
        }
    }

    public List<Review> getReviewsByCourseId(int courseId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String query = "SELECT * FROM Reviews WHERE CourseID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, courseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("ReviewID");
                    int userId = resultSet.getInt("UserID");
                    int rating = resultSet.getInt("Rating");
                    String comment = resultSet.getString("Comment");
                    Timestamp timestamp = resultSet.getTimestamp("Timestamp");
                    reviews.add(new Review(id, courseId, userId, rating, comment, timestamp));
                }
            }
        }
        return reviews;
    }

    public void deleteReviewByUserAndCourse(int userId, int courseId) throws SQLException {
        String deleteQuery = "DELETE FROM Reviews WHERE UserID = ? AND CourseID = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setInt(1, userId);
            statement.setInt(2, courseId);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("Error deleting review", e);
        }
    }

    public double updateAverageRating(int courseId) throws SQLException {
        double ret = 0.0;
        String query = "SELECT AVG(Rating) as AverageRating FROM Reviews WHERE CourseID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, courseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double averageRating = resultSet.getDouble("AverageRating");
                    ret = averageRating;
                    String updateQuery = "UPDATE Courses SET AverageRating = ? WHERE CourseID = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        updateStatement.setDouble(1, averageRating);
                        updateStatement.setInt(2, courseId);
                        updateStatement.executeUpdate();
                        connection.commit();
                    }
                }
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
        return ret;
    }

    // Adds review to DB if none exists for username/course combo, otherwise updates
    public void addOrUpdateReview(Review review) throws SQLException {
        String checkExistingReview = "SELECT ReviewID FROM Reviews WHERE CourseID = ? AND UserID = ?";
        String deleteExistingReview = "DELETE FROM Reviews WHERE ReviewID = ?";
        String addNewReview = "INSERT INTO Reviews (ReviewID, CourseID, UserID, Rating, Comment, Timestamp) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkExistingReview)) {
            checkStmt.setInt(1, review.getCourseId());
            checkStmt.setInt(2, review.getUserId());

            try (ResultSet resultSet = checkStmt.executeQuery()) {
                if (resultSet.next()) {
                    // Review exists; delete it
                    int existingReviewId = resultSet.getInt("ReviewID");

                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteExistingReview)) {
                        deleteStmt.setInt(1, existingReviewId);
                        deleteStmt.executeUpdate();
                    }
                }
            }
        }
        // Add the new review
        try (PreparedStatement insertStmt = connection.prepareStatement(addNewReview)) {
            int uniqueID = generateUniqueReviewID();
            insertStmt.setInt(1, uniqueID);
            insertStmt.setInt(2, review.getCourseId());
            insertStmt.setInt(3, review.getUserId());
            insertStmt.setInt(4, review.getRating());
            insertStmt.setString(5, review.getComment());
            insertStmt.setTimestamp(6, review.getTimestamp());

            insertStmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public Course getTableSelectedCourse(String mnemonic, int courseNumber, String title) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String getTableCourseQuery = "SELECT * FROM Courses WHERE " +
                "(UPPER(SubjectMnemonic) = UPPER(?)) AND " +
                "(CourseNumber = ?) AND " +
                "(UPPER(CourseTitle) = UPPER(?))";

        try (PreparedStatement statement = connection.prepareStatement(getTableCourseQuery)) {
            // Set parameters for SubjectMnemonic
            statement.setString(1, mnemonic);
            statement.setInt(2, courseNumber);
            statement.setString(3, title);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    int courseID = resultSet.getInt("CourseID");
                    String subjectMnemonic = resultSet.getString("SubjectMnemonic");
                    int courseNum = resultSet.getInt("CourseNumber");
                    String courseTitle = resultSet.getString("CourseTitle");
                    double averageRating = resultSet.getDouble("AverageRating");

                    return new Course(courseID, subjectMnemonic, courseNum, courseTitle, averageRating);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error retrieving courses", e);
        }
    }

    public List<Course> searchCourses(String mnemonic, int courseNumber, String title) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String getCoursesQuery = "SELECT * FROM Courses WHERE " +
                "(UPPER(SubjectMnemonic) = UPPER(?) OR ? IS NULL) AND " +
                "(CourseNumber = ? OR ? = 0) AND " +
                "(UPPER(CourseTitle) LIKE UPPER(?) OR ? IS NULL)";

        try (PreparedStatement statement = connection.prepareStatement(getCoursesQuery)) {
            //Source: https://blogs.oracle.com/javamagazine/post/quiz-yourself-working-with-preparedstatement-and-sql-null-values-in-java
            //From lines 341 - 363, helped me figure out how to get the search function to work with the database when search fields are left empty
            if (mnemonic.isEmpty()) {
                statement.setNull(1, Types.VARCHAR);
                statement.setNull(2, Types.VARCHAR);
            } else {
                statement.setString(1, mnemonic);
                statement.setString(2, mnemonic);
            }

            if (courseNumber == 0) {
                statement.setNull(3, Types.INTEGER);
                statement.setInt(4, courseNumber);
            } else {
                statement.setInt(3, courseNumber);
                statement.setInt(4, courseNumber);
            }

            if (title.isEmpty()) {
                statement.setNull(5, Types.VARCHAR);
                statement.setNull(6, Types.VARCHAR);
            } else {
                statement.setString(5, "%" + title + "%");
                statement.setString(6, "%" + title + "%");
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int courseID = resultSet.getInt("CourseID");
                    String subjectMnemonic = resultSet.getString("SubjectMnemonic");
                    int courseNum = resultSet.getInt("CourseNumber");
                    String courseTitle = resultSet.getString("CourseTitle");
                    double averageRating = resultSet.getDouble("AverageRating");
                    courses.add(new Course(courseID, subjectMnemonic, courseNum, courseTitle, averageRating));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error retrieving courses", e);
        }

        return courses;
    }

    public boolean validateCourses(String mnemonic, int courseNumber, String title) throws SQLException {
        String getCoursesQuery = "SELECT 1 FROM Courses WHERE " +
                "UPPER(SubjectMnemonic) = UPPER(?) AND " +
                "CourseNumber = ? AND " +
                "UPPER(CourseTitle) LIKE UPPER(?)";

        try (PreparedStatement statement = connection.prepareStatement(getCoursesQuery)) {
            statement.setString(1, mnemonic);
            statement.setInt(2, courseNumber);
            statement.setString(3, "%" + title + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                return !resultSet.next();
            }
        } catch (SQLException e) {
            throw new SQLException("Error validating courses", e);
        }
    }

    public void clearDatabase() throws SQLException {
        String deleteUsersQuery = "DELETE FROM Users";
        String deleteCoursesQuery = "DELETE FROM Courses";
        String deleteReviewsQuery = "DELETE FROM Reviews";

        try (Statement statement = connection.createStatement()) {
            statement.execute(deleteUsersQuery);
            statement.executeUpdate(deleteCoursesQuery);
            statement.executeUpdate(deleteReviewsQuery);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("Error clearing database", e);
        }
    }

    private int generateUniqueUserID() throws SQLException {
        String query = "SELECT MAX(UserID) FROM Users";
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    private int generateUniqueReviewID() throws SQLException {
        String query = "SELECT MAX(ReviewID) FROM Reviews";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    private int generateUniqueCourseID() throws SQLException {
        String query = "SELECT MAX(CourseID) FROM Courses";
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public static void main(String[] args) {
        try {
            CourseReviewDatabase database = CourseReviewDatabase.getInstance();
            database.clearDatabase();
            // Example of creating users
            database.createUser(new User(0, "User1234", "Pass5678"));
            database.createUser(new User(1, "User2345", "Pass6789"));

            System.out.println("Users created successfully.");

            // Check if a username exists
            boolean exists = database.userNameExists("User1234");
            System.out.println("Does User1234 exist? " + exists);

            // Add sample courses
            database.addCourse(new Course(0, "CS", 1000, "Intro to Programming", 4.5));
            database.addCourse(new Course(0, "MATH", 2020, "Calculus II", 4.2));
            database.addCourse(new Course(0, "PHYS", 3030, "Physics III", 4.8));

            System.out.println("Courses added successfully.");

            // Retrieve and print all courses
            List<Course> courses = database.searchCourses("", 0, "");
            for (Course course : courses) {
                System.out.println("Course ID: " + course.getCourseID() +
                        ", Subject: " + course.getSubjectMnemonic() +
                        ", Course Number: " + course.getCourseNumber() +
                        ", Title: " + course.getCourseTitle() +
                        ", Average Rating: " + course.getAverageRating());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

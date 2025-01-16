package edu.virginia.sde.reviews;

import java.sql.Timestamp;

public class ExtendedReview extends Review {
    private String subjectMnemonic;
    private int courseNumber;

    public ExtendedReview(int reviewID, int courseID, int userID, int rating, String comment, Timestamp timestamp, String subjectMnemonic, int courseNumber) {
        super(reviewID, courseID, userID, rating, comment, timestamp);
        this.subjectMnemonic = subjectMnemonic;
        this.courseNumber = courseNumber;
    }

    public String getSubjectMnemonic() {
        return subjectMnemonic;
    }

    public int getCourseNumber() {
        return courseNumber;
    }
}
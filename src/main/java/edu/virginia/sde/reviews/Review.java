package edu.virginia.sde.reviews;

import java.sql.Timestamp;

public class Review {
    private int id;
    private int courseId;
    private int userId;
    private int rating;
    private String comment;
    private Timestamp timestamp;

    public Review(int i, int c, int u, int r, String cm, Timestamp t){
        this.id = i;
        this.courseId = c;
        this.userId = u;
        this.rating = r;
        this.comment = cm;
        this.timestamp = t;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}

package edu.virginia.sde.reviews;

public class CourseService {
    private static Course currentCourse;

    // Sets the current course
    public static void setCurrentCourse(Course course) {
       currentCourse = course;
    }

    // Gets the current course
    public static Course getCurrentCourse() {
        return currentCourse;
    }
}
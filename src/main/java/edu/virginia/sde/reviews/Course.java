package edu.virginia.sde.reviews;
import java.util.ArrayList;
import java.util.List;

public class Course {
    private int courseID;
    private String subjectMnemonic;
    private int courseNumber;
    private String courseTitle;
    private Double averageRating;

    public Course(int courseID, String subjectMnemonic, int courseNumber, String courseTitle, Double averageRating)
    {
        List<String> errorsFound = new ArrayList<>();
        if (!CourseValidator.isValidMnemonic(subjectMnemonic))
        {
            errorsFound.add("Subject Mnemonic must be between 2 and 4 letters");
        }
        if (!CourseValidator.isValidCourseNumber(Integer.toString(courseNumber)))
        {
            errorsFound.add("Course number must be exactly 4 digits");
        }
        if (!CourseValidator.isValidCourseTitle(courseTitle))
        {
            errorsFound.add("Course title must be between 1 and 50 characters");
        }
        if (!errorsFound.isEmpty())
        {
            throw new IllegalArgumentException(String.join(" + ", errorsFound));
        }
        this.courseID = courseID;
        this.subjectMnemonic = subjectMnemonic.toUpperCase();
        this.courseNumber = courseNumber;
        this.courseTitle = courseTitle;
        this.averageRating = averageRating;
    }

    public int getCourseID() {
        return courseID;
    }
    public String getSubjectMnemonic()
    {
        return subjectMnemonic;
    }
    public int getCourseNumber(){

        return courseNumber;
    }
    public String getCourseTitle(){

        return courseTitle;
    }
    public Double getAverageRating(){
        return averageRating;
    }

}




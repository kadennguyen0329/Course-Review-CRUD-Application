package edu.virginia.sde.reviews;

public class CourseValidator {
    public static boolean isValidMnemonic(String mnemonic)
    {
        return mnemonic != null && mnemonic.length() >= 2 && mnemonic.length() <= 4 && isLetters(mnemonic);
    }
    public static boolean isValidCourseNumber(String courseNumber)
    {
        return courseNumber.length() == 4 && isNumbers(courseNumber);
    }
    public static boolean isValidCourseTitle(String title) {
        return title != null && !title.isEmpty() && title.length() <= 50;
    }
    public static boolean isNumbers(String str)
    {
        for (char c : str.toCharArray())
        {
            if (!Character.isDigit(c))
            {
                return false;
            }
        }
        return true;
    }
    public static boolean isLetters(String str)
    {
        for (char c : str.toCharArray())
        {
            if (!Character.isLetter(c))
            {
                return false;
            }
        }
        return true;
    }
}

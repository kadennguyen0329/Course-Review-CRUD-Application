package edu.virginia.sde.reviews;

public class User {

    private final String username;
    private final String password;
    private final int id;

    public User(int id, String username, String password)
    {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public int getId(){return id;}
}

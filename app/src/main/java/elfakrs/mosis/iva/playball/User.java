package elfakrs.mosis.iva.playball;
import java.util.ArrayList;

public class User {

    private String id;
    private String email;
    private String password;
    private String name;
    private String photo;
    private int radius;
    private String phoneNumber;
    private String sex;
    private String favoriteSport;
    private String city;
    private int age;
    private float score;
    private ArrayList<String> friendsID;
    private ArrayList<String> commentsID;
    private ArrayList<String> gamesID;
    private ArrayList<String> goingGamesID;

    public User(){

        friendsID = new ArrayList<String>();
        commentsID = new ArrayList<String >();
        gamesID = new ArrayList<String>();
        goingGamesID = new ArrayList<String>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getFavoriteSport() {
        return favoriteSport;
    }

    public void setFavoriteSport(String favoriteSport) {
        this.favoriteSport = favoriteSport;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public ArrayList<String> getFriendsID() {
        return friendsID;
    }

    public void setFriendsID(ArrayList<String> friendsID) {
        this.friendsID = friendsID;
    }

    public ArrayList<String> getCommentsID() {
        return commentsID;
    }

    public void setCommentsID(ArrayList<String> commentsID) {
        this.commentsID = commentsID;
    }

    public ArrayList<String> getGamesID() {
        return gamesID;
    }

    public void setGamesID(ArrayList<String> gamesID) {
        this.gamesID = gamesID;
    }

    public ArrayList<String> getGoingGamesID() {
        return goingGamesID;
    }

    public void setGoingGamesID(ArrayList<String> goingGamesID) {
        this.goingGamesID = goingGamesID;
    }

}

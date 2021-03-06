package elfakrs.mosis.iva.playball.Model;
import java.util.ArrayList;

public class User {

    private String id;
    private String email;
    private String password;
    private String name;
    private int radius;
    private String sex;
    private String favoriteSport;
    private String city;
    private int age;
    private float score;
    private int numOfRatings;
    private String longitude;
    private String latitude;
    private boolean notifications;
    private boolean location;
    private boolean football;
    private boolean basketball;
    private boolean volleyball;
    private boolean others;
    private boolean hasImg;
    private float mass;

    private ArrayList<String> friendsID;
    private ArrayList<String> commentsID;
    private ArrayList<String> gamesID;
    private ArrayList<String> goingGamesID;
    private ArrayList<String> notifiedGames;
    private ArrayList<String> myGames;

    public User(){
        friendsID = new ArrayList<>();
        commentsID = new ArrayList< >();
        gamesID = new ArrayList<>();
        goingGamesID = new ArrayList<>();
        notifiedGames = new ArrayList<>();
        myGames = new ArrayList<>();
        mass = 0;
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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
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

    public int getNumOfRatings() {
        return numOfRatings;
    }

    public void setNumOfRatings(int numOfRatings) {
        this.numOfRatings = numOfRatings;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public boolean isLocation() {
        return location;
    }

    public void setLocation(boolean location) {
        this.location = location;
    }

    public void addFriend(String id)
    {
        friendsID.add(id);
    }

    public void addComment(String id)
    {
        commentsID.add(id);
    }

    public void addCreatedGame(String id)
    {
        gamesID.add(id);
    }

    public void addGoingGame(String id)
    {
        goingGamesID.add(id);
    }

    public boolean isFootball() {
        return football;
    }

    public void setFootball(boolean football) {
        this.football = football;
    }

    public boolean isBasketball() {
        return basketball;
    }

    public void setBasketball(boolean basketball) {
        this.basketball = basketball;
    }

    public boolean isVolleyball() {
        return volleyball;
    }

    public void setVolleyball(boolean volleyball) {
        this.volleyball = volleyball;
    }

    public boolean isOthers() {
        return others;
    }

    public void setOthers(boolean others) {
        this.others = others;
    }

    public boolean isHasImg() {
        return hasImg;
    }

    public void setHasImg(boolean hasImg) {
        this.hasImg = hasImg;
    }

    public ArrayList<String> getNotifiedGames() {
        return notifiedGames;
    }

    public void setNotifiedGames(ArrayList<String> notifiedGames) {
        this.notifiedGames = notifiedGames;
    }

    public void addNotifiedGame(String gameID){
        notifiedGames.add(gameID);
    }

    public ArrayList<String> getMyGames() {
        return myGames;
    }

    public void setMyGames(ArrayList<String> myGames) {
        this.myGames = myGames;
    }

    public void addMyGame(String gameID){
        this.myGames.add(gameID);
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }


}

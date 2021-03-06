package elfakrs.mosis.iva.playball.Model;

import java.util.ArrayList;
import java.util.Date;

public class Game {

    private String id;
    private String sport;
    private Date dateTime;
    private String latitude;
    private String longitude;
    private ArrayList<String> goingUsersID;
    private String creatorID;
    private String tittle;
    private boolean hasImg;

    public Game() {
        dateTime = new Date();
        goingUsersID = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public ArrayList<String> getGoingUsersID() {
        return goingUsersID;
    }

    public void setGoingUsersID(ArrayList<String> goingUsersID) {
        this.goingUsersID = goingUsersID;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String photoURI) {
        this.tittle = photoURI;
    }

    public void addGoingUser(String id)
    {
        goingUsersID.add(id);
    }

    public boolean isHasImg() {
        return hasImg;
    }

    public void setHasImg(boolean hasImg) {
        this.hasImg = hasImg;
    }

}

package elfakrs.mosis.iva.playball.Model;

import java.util.ArrayList;
import java.util.Date;

public class MyGame {

    private String id;
    private String name;
    private String userID;
    private ArrayList<Integer> heartBeats;
    private int steps;
    private int duration; //in seconds

    private Date date;

    public MyGame(){
        heartBeats = new ArrayList<>();
        date = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public ArrayList<Integer> getHeartBeats() {
        return heartBeats;
    }

    public void setHeartBeats(ArrayList<Integer> heartBeats) {
        this.heartBeats = heartBeats;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void addHeartBeat(int heartBeat){
        this.heartBeats.add(heartBeat);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}

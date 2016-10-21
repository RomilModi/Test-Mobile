package com.example.rmodi.checkconstantlocation.bean;

/**
 * Created by rmodi on 10/21/2016.
 */

public class PunchInOutbean {

    public int ID;
    public String USER_ID;
    public String USER_ACTIVITY;
    public String DATE;
    public String LATITUDE;
    public String LONGITUDE;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getUSER_ID() {
        return USER_ID;
    }

    public void setUSER_ID(String USER_ID) {
        this.USER_ID = USER_ID;
    }

    public String getUSER_ACTIVITY() {
        return USER_ACTIVITY;
    }

    public void setUSER_ACTIVITY(String USER_ACTIVITY) {
        this.USER_ACTIVITY = USER_ACTIVITY;
    }

    public String getDATE() {
        return DATE;
    }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }

    public String getLATITUDE() {
        return LATITUDE;
    }

    public void setLATITUDE(String LATITUDE) {
        this.LATITUDE = LATITUDE;
    }

    public String getLONGITUDE() {
        return LONGITUDE;
    }

    public void setLONGITUDE(String LONGITUDE) {
        this.LONGITUDE = LONGITUDE;
    }
}

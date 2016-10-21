package com.example.rmodi.checkconstantlocation.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmodi on 10/21/2016.
 */

public class Data {
    @SerializedName("userAttendance")
    public List<UserAttendance> userAttendance = new ArrayList<UserAttendance>();
}

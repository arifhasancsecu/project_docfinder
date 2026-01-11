package com.find.doc.model;

import java.util.List;

public class Clinic {
    private String clinicName;
    private List<String> hotlines;
    private String location;
    public Clinic() { }

    public Clinic(String clinicName, List<String> hotlines, String location) {
        this.clinicName = clinicName;
        this.hotlines = hotlines;
        this.location = location;
    }

    public String getClinicName() { return clinicName; }

    public List<String> getHotlines() { return hotlines; }

    public String getLocation() { return location; }
}
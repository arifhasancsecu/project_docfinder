package com.titukumar.doctor.model;

import java.io.Serializable;

public class Doctor implements Serializable {

    private String image;
    private String name;
    private String degree;
    private String category;
    private float popularity;
    private String hospital;
    private String location;
    private Object serialNumber;
    private float visitingFee;

    private String doctorId;


    public Doctor() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getPopularity() {
        return popularity;
    }

    public void setPopularity(float popularity) {
        this.popularity = popularity;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public Object getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(Object serialNumber) {
        this.serialNumber = serialNumber;
    }

    public float getVisitingFee() {
        return visitingFee;
    }

    public void setVisitingFee(float visitingFee) {
        this.visitingFee = visitingFee;
    }
    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

}


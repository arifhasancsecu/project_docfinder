package com.find.doc.model;

public class DoctorAppointment {
    private Doctor doctor;

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    private String appointmentDate;

    public DoctorAppointment() { }

    public DoctorAppointment(Doctor doctor, String appointmentDate) {
        this.doctor = doctor;
        this.appointmentDate = appointmentDate;
    }

    public Doctor getDoctor() { return doctor; }
    public String getAppointmentDate() { return appointmentDate; }
}


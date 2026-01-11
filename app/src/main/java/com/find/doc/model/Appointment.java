package com.find.doc.model;

public class Appointment {
    public Appointment(String appointment_date, long created_at, long updated_at) {
        this.appointment_date = appointment_date;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    private String appointment_date;
    private long created_at;
    private long updated_at;

    public Appointment() {}

    public String getAppointment_date() { return appointment_date; }
    public void setAppointment_date(String appointment_date) { this.appointment_date = appointment_date; }

    public long getCreated_at() { return created_at; }
    public void setCreated_at(long created_at) { this.created_at = created_at; }

    public long getUpdated_at() { return updated_at; }
    public void setUpdated_at(long updated_at) { this.updated_at = updated_at; }
}

package com.find.doc.util;

import com.find.doc.model.Doctor;
import java.util.ArrayList;
import java.util.List;

public class DoctorRepository {
    private static DoctorRepository instance;
    private List<Doctor> doctorList = new ArrayList<>();

    private DoctorRepository() {}

    public static DoctorRepository getInstance() {
        if (instance == null) {
            instance = new DoctorRepository();
        }
        return instance;
    }

    public List<Doctor> getDoctorList() {
        return doctorList;
    }

    public void setDoctorList(List<Doctor> doctors) {
        this.doctorList = doctors;
    }
}

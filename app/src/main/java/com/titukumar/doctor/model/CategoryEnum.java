package com.titukumar.doctor.model;
import com.titukumar.doctor.R;


public enum CategoryEnum {
    CARDIOLOGY("Cardiology", R.drawable.cardiology),
    NEUROLOGY(" Neurology", R.drawable.neurology),
    CANCER_SPECIALIST("Cancer Specialist", R.drawable.cancer),
    DENTAL("Dental", R.drawable.dental),
    MEDICINE("Medicine", R.drawable.medicine),
    DERMATOLOGY("Dermatology", R.drawable.dermatology),
    DIABETES("Diabetes & Hormone", R.drawable.diabetes),
    ENT("NoseEar Throat", R.drawable.ent),
    GASTROENTEROLOGY("Gastroenterology", R.drawable.gastroenterology),
    GYNECOLOGY("Gynocology", R.drawable.gynocology),
    HEART("Cardiology & Heart specialist", R.drawable.heart_sp),
    HEMATOLOGY("Hematology", R.drawable.hematology),
    LAPAROSCOPICPEDIATRIC("Laparoscopic & pediatric ", R.drawable.lappedsurgeon),
    LAPAROSCOPICSURGEON("Laparoscopic surgeon", R.drawable.lapsurg),
    LIVERSPECIALIST("Liver specialist", R.drawable.liver),
    MEDICINECARDIOLOGY("Medicine & Cardiology", R.drawable.medicinecardiology),
    NEPHROLOGY("Nephrology", R.drawable.nephrology),
    NEUROSPINE("Neuro & Spine Surgery", R.drawable.neurospine),
    NEUROLOGYMEDICINE("Neurology & Medicine", R.drawable.nmedicine),
    OPHTHALMOLOGY("Eye specialist", R.drawable.ophthalmology),
    ORTHOPEDICSPINE("Orthopedic & Spine Surgery", R.drawable.orthopedics),
    ORTHOPEDICSURGERY("Orthopedic Surgery", R.drawable.othosurgery),
    PARALYSIS("Paralysis & Physical Medicine", R.drawable.phyedicine),
    PEDIATRICMEDICINE("Pediatric Medicine", R.drawable.pedicine),
    PSYCHIATRY("Psychiatry", R.drawable.psychiatry),
    SURGERY("Neuro Surgery", R.drawable.nerurgery),
    UROLOGY("Urology", R.drawable.urology);


    private final String displayName;
    private final int imageResId;

    CategoryEnum(String displayName, int imageResId) {
        this.displayName = displayName;
        this.imageResId = imageResId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getImageResId() {
        return imageResId;
    }

    public boolean matches(String query) {
        return displayName.toLowerCase().contains(query.toLowerCase());
    }
}

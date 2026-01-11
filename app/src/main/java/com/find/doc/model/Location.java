package com.find.doc.model;

public enum Location {

    SYLHET(1, "Sylhet"),
    DHAKA(2, "Dhaka"),
    MOULVIBAZAR(3, "Moulvibazar"),
    BARLEKHA(4, "Barlekha"),
    JURI(5, "Juri"),
    KULAURA(6, "Kulaura"),
    BEANIBAZAR(7, "Beanibazar"),
    GOLAPGANJ(8, "Golapganj"),
    ZAIGANJ(9, "Zakiganj"),
    KANAIGHAT(10, "Kanaighat"),
    CHARKHAI(10, "Charkhai"),
    COMPANIGANJ(11, "Companiganj"),
    DAKSHINSHURMA(12, "Dakshin Surma"),
    BISHWANATH(13, "Bishwanath"),
    CHITTAGONG(14, "Chittagong");

    private int position;
    private String displayName;

    Location(int position, String displayName) {
        this.position = position;
        this.displayName = displayName;
    }

    public int getPosition() {
        return position;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Location fromDisplayName(String name) {
        for (Location loc : Location.values()) {
            if (loc.getDisplayName().equalsIgnoreCase(name)) {
                return loc;
            }
        }
        return null;
    }


    public static Location getLocation(int position) {
        switch (position) {
            case 0:
                return SYLHET;

            case 1:
                return DHAKA;

            default:
                return null;
        }
    }
}



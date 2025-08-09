package com.pes.copa.tournaments.enums;

public enum TeamType {
    COUNTRY("Selección"),
    CLUB("Club");
    
    private final String displayName;
    
    TeamType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
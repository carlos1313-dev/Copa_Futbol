package com.pes.copa.tournaments.enums;

public enum TournamentType {
    WORLD_CUP("Mundial"),
    COPA_AMERICA("Copa América"),
    EURO("Eurocopa"),
    CHAMPIONS_LEAGUE("Champions League"),
    CUSTOM_COUNTRIES("Selecciones Personalizadas"),
    CUSTOM_CLUBS("Clubes Personalizados");
    
    private final String displayName;
    
    TournamentType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
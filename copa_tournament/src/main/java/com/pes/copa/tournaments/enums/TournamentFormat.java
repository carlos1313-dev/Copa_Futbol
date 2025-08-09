package com.pes.copa.tournaments.enums;

public enum TournamentFormat {
    GROUPS_THEN_KNOCKOUT("Fase de Grupos + Eliminatorias"),
    DIRECT_KNOCKOUT("Eliminación Directa");
    
    private final String displayName;
    
    TournamentFormat(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
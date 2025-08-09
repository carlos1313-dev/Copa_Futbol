package com.pes.copa.tournaments.enums;

public enum TournamentStatus {
    WAITING_PLAYERS("Esperando jugadores"),
    CREATED("Creado"),
    IN_PROGRESS("En Progreso"),
    FINISHED("Finalizado");
    
    private final String displayName;
    
    TournamentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
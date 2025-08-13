package com.pes.copa.matches.dto.response;

public class TournamentSummaryDTO {
    private Long tournamentId;
    private Integer totalMatches;
    private Integer finishedMatches;
    private Integer pendingMatches;
    private Double completionPercentage;

    public Long getTournamentId() { return tournamentId; }
    public void setTournamentId(Long tournamentId) { this.tournamentId = tournamentId; }

    public Integer getTotalMatches() { return totalMatches; }
    public void setTotalMatches(Integer totalMatches) { this.totalMatches = totalMatches; }

    public Integer getFinishedMatches() { return finishedMatches; }
    public void setFinishedMatches(Integer finishedMatches) { this.finishedMatches = finishedMatches; }

    public Integer getPendingMatches() { return pendingMatches; }
    public void setPendingMatches(Integer pendingMatches) { this.pendingMatches = pendingMatches; }

    public Double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Double completionPercentage) { this.completionPercentage = completionPercentage; }
}

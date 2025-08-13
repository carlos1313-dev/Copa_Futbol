package com.pes.copa.matches.controller;

import com.pes.copa.matches.dto.request.SimulateMatchesDTO;
import com.pes.copa.matches.dto.request.SubmitMatchResultDTO;
import com.pes.copa.matches.dto.response.*;
import com.pes.copa.matches.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/matches")
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchService matchService;

    @Autowired
    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    
    // GENERACIÓN DE PARTIDOS
    @PostMapping("/tournaments/{tournamentId}/generate")
    public ResponseEntity<String> generateTournamentMatches(@PathVariable Long tournamentId) {
        try {
            matchService.generateMatches(tournamentId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Partidos generados para el torneo " + tournamentId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // CONSULTA DE PARTIDOS
    @GetMapping("/tournaments/{tournamentId}")
    public ResponseEntity<List<MatchDTO>> getTournamentMatches(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(matchService.getTournamentMatches(tournamentId));
    }

    @GetMapping("/tournaments/{tournamentId}/pending")
    public ResponseEntity<List<PendingMatchesDTO>> getAllPendingMatches(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(matchService.getAllPendingMatches(tournamentId));
    }

    @GetMapping("/tournaments/{tournamentId}/players/{playerId}/pending")
    public ResponseEntity<List<MatchDTO>> getPlayerPendingMatches(
            @PathVariable Long tournamentId,
            @PathVariable Long playerId) {
        return ResponseEntity.ok(matchService.getPendingMatchesByPlayer(playerId, tournamentId));
    }

    @GetMapping("/tournaments/{tournamentId}/groups")
    public ResponseEntity<List<GroupStandingDTO>> getGroupStandings(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(matchService.getGroupStandings(tournamentId));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchDTO> getMatchDetails(@PathVariable Long matchId) {
        // Implementar lógica para obtener partido por ID
        return ResponseEntity.notFound().build();
    }


    // REGISTRO DE RESULTADOS
    @PostMapping("/results")
    public ResponseEntity<?> submitMatchResult(@Valid @RequestBody SubmitMatchResultDTO resultDTO) {
        try {
            MatchDTO updatedMatch = matchService.submitMatchResult(resultDTO);
            return ResponseEntity.ok(updatedMatch);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // SIMULACIÓN
    @PostMapping("/simulate")
    public ResponseEntity<List<MatchDTO>> simulateMatches(@Valid @RequestBody SimulateMatchesDTO simulateDTO) {
        try {
            return ResponseEntity.ok(matchService.simulateMatches(simulateDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/tournaments/{tournamentId}/simulate-all")
    public ResponseEntity<List<MatchDTO>> simulateAllAIMatches(@PathVariable Long tournamentId) {
        SimulateMatchesDTO dto = new SimulateMatchesDTO();
        dto.setTournamentId(tournamentId);
        return ResponseEntity.ok(matchService.simulateMatches(dto));
    }

    @PostMapping("/tournaments/{tournamentId}/phases/{phase}/simulate")
    public ResponseEntity<List<MatchDTO>> simulatePhaseMatches(
            @PathVariable Long tournamentId,
            @PathVariable String phase) {
        SimulateMatchesDTO dto = new SimulateMatchesDTO();
        dto.setTournamentId(tournamentId);
        dto.setPhase(phase);
        return ResponseEntity.ok(matchService.simulateMatches(dto));
    }

    @PostMapping("/tournaments/{tournamentId}/groups/{groupName}/simulate")
    public ResponseEntity<List<MatchDTO>> simulateGroupMatches(
            @PathVariable Long tournamentId,
            @PathVariable String groupName) {
        SimulateMatchesDTO dto = new SimulateMatchesDTO();
        dto.setTournamentId(tournamentId);
        dto.setGroupName(groupName);
        return ResponseEntity.ok(matchService.simulateMatches(dto));
    }

    // PROGRESIÓN DEL TORNEO
    @PostMapping("/tournaments/{tournamentId}/advance-groups")
    public ResponseEntity<String> advanceFromGroupStage(@PathVariable Long tournamentId) {
        try {
            matchService.advanceFromGroupStage(tournamentId);
            return ResponseEntity.ok("Equipos avanzados a eliminatorias");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ESTADÍSTICAS
    @GetMapping("/tournaments/{tournamentId}/summary")
    public ResponseEntity<TournamentSummaryDTO> getTournamentSummary(@PathVariable Long tournamentId) {
        TournamentSummaryDTO summary = new TournamentSummaryDTO();
        List<MatchDTO> allMatches = matchService.getTournamentMatches(tournamentId);

        summary.setTournamentId(tournamentId);
        summary.setTotalMatches(allMatches.size());
        summary.setFinishedMatches((int) allMatches.stream()
                .filter(m -> "FINISHED".equals(m.getStatus()) || "SIMULATED".equals(m.getStatus())).count());
        summary.setPendingMatches((int) allMatches.stream()
                .filter(m -> "PENDING".equals(m.getStatus())).count());
        summary.setCompletionPercentage(summary.getTotalMatches() > 0 ?
                (summary.getFinishedMatches() * 100.0) / summary.getTotalMatches() : 0.0);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/tournaments/{tournamentId}/current-phase")
    public ResponseEntity<CurrentPhaseDTO> getCurrentPhase(@PathVariable Long tournamentId) {
        List<MatchDTO> allMatches = matchService.getTournamentMatches(tournamentId);
        CurrentPhaseDTO currentPhase = new CurrentPhaseDTO();
        currentPhase.setTournamentId(tournamentId);

        boolean hasGroupStage = allMatches.stream()
                .anyMatch(m -> "GROUP_STAGE".equals(m.getPhase()));

        if (hasGroupStage) {
            boolean groupStageComplete = allMatches.stream()
                    .filter(m -> "GROUP_STAGE".equals(m.getPhase()))
                    .allMatch(m -> "FINISHED".equals(m.getStatus()) || "SIMULATED".equals(m.getStatus()));

            if (!groupStageComplete) {
                currentPhase.setCurrentPhase("GROUP_STAGE");
                currentPhase.setPhaseName("Fase de Grupos");
            } else {
                String[] phases = {"ROUND_16", "QUARTER_FINAL", "SEMI_FINAL", "THIRD_PLACE", "FINAL"};
                for (String phase : phases) {
                    boolean phaseHasMatches = allMatches.stream().anyMatch(m -> phase.equals(m.getPhase()));
                    if (phaseHasMatches) {
                        boolean phaseComplete = allMatches.stream()
                                .filter(m -> phase.equals(m.getPhase()))
                                .allMatch(m -> "FINISHED".equals(m.getStatus()) || "SIMULATED".equals(m.getStatus()));
                        if (!phaseComplete) {
                            currentPhase.setCurrentPhase(phase);
                            currentPhase.setPhaseName(getPhaseDisplayName(phase));
                            break;
                        }
                    }
                }
            }
        }

        return ResponseEntity.ok(currentPhase);
    }

    private String getPhaseDisplayName(String phase) {
        return switch (phase) {
            case "GROUP_STAGE" -> "Fase de Grupos";
            case "ROUND_16" -> "Octavos de Final";
            case "QUARTER_FINAL" -> "Cuartos de Final";
            case "SEMI_FINAL" -> "Semifinal";
            case "THIRD_PLACE" -> "Tercer Lugar";
            case "FINAL" -> "Final";
            default -> phase;
        };
    }
}

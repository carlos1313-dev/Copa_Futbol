package com.pes.copa.tournaments.service;

import com.pes.copa.tournaments.dto.request.CreateTournamentDTO;
import com.pes.copa.tournaments.dto.response.TournamentDTO;
import com.pes.copa.tournaments.entity.Tournament;
import com.pes.copa.tournaments.enums.TournamentFormat;
import com.pes.copa.tournaments.enums.TournamentStatus;
import com.pes.copa.tournaments.enums.TournamentType;
import com.pes.copa.tournaments.mapper.TournamentMapper;
import com.pes.copa.tournaments.repository.TournamentRepository;
import com.pes.copa.tournaments.repository.TournamentTeamRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar las operaciones relacionadas con torneos
 *
 * @author sangr
 */
@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentTeamRepository tournamentTeamRepository;
    private final TournamentMapper tournamentMapper;

    @Autowired
    public TournamentService(TournamentRepository tournamentRepository,
            TournamentTeamRepository tournamentTeamRepository,
            TournamentMapper tournamentMapper) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
        this.tournamentMapper = tournamentMapper;
    }

    /**
     * Crea un nuevo torneo
     *
     * @param dto datos para crear el torneo
     * @return torneo creado
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    @Transactional
    public TournamentDTO createTournament(CreateTournamentDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del torneo no pueden ser nulos");
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del torneo no puede ser nulo o vacío");
        }

        if (dto.getNumTeams() == null || dto.getNumTeams() < 4 || dto.getNumTeams() > 32) {
            throw new IllegalArgumentException("El número de equipos debe estar entre 4 y 32");
        }

        // Validar que numTeams sea potencia de 2 para eliminatorias directas
        if (dto.getTournamentFormat() == TournamentFormat.DIRECT_KNOCKOUT
                && !isPowerOfTwo(dto.getNumTeams())) {
            throw new IllegalArgumentException("Para eliminación directa, el número de equipos debe ser potencia de 2 (4, 8, 16, 32)");
        }

        // Validar que numTeams sea múltiplo de 4 para fase de grupos
        if (dto.getTournamentFormat() == TournamentFormat.GROUPS_THEN_KNOCKOUT
                && dto.getNumTeams() % 4 != 0) {
            throw new IllegalArgumentException("Para fase de grupos, el número de equipos debe ser múltiplo de 4");
        }

        try {
            Tournament tournament = tournamentMapper.dtoToEntity(dto);
            tournament.setStatus(TournamentStatus.CREATED);
            tournament.setNumPlayers(0); // Iniciar sin jugadores
            tournament.setCreatedDate(LocalDateTime.now());

            Tournament saved = tournamentRepository.save(tournament);
            return tournamentMapper.entityToDTO(saved);

        } catch (DataAccessException e) {
            throw new RuntimeException("Error al crear el torneo en la base de datos", e);
        }
    }

    /**
     * Método auxiliar para validar si un número es potencia de 2
     *
     * @param n número a validar
     * @return true si es potencia de 2
     */
    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    /**
     * Obtiene un torneo por su ID
     *
     * @param id identificador del torneo
     * @return el torneo encontrado
     * @throws IllegalArgumentException si el ID es nulo
     * @throws RuntimeException si no se encuentra el torneo o hay problemas de
     * BD
     */
    public TournamentDTO getTournamentById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        try {
            Optional<Tournament> tournament = tournamentRepository.findById(id);
            if (tournament.isEmpty()) {
                throw new RuntimeException("Torneo no encontrado con ID: " + id);
            }
            return tournamentMapper.entityToDTO(tournament.get());
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Obtiene todos los torneos por estado
     *
     * @param status estado del torneo
     * @return lista de torneos
     * @throws IllegalArgumentException si el estado es nulo
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public List<TournamentDTO> getTournamentsByStatus(TournamentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }

        try {
            List<Tournament> tournaments = tournamentRepository.findByStatus(status);
            return tournaments.stream()
                    .map(tournamentMapper::entityToDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Obtiene torneos creados por un usuario específico
     *
     * @param userId ID del usuario
     * @return lista de torneos del usuario
     * @throws IllegalArgumentException si el userId es nulo
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public List<TournamentDTO> getTournamentsByUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("El ID de usuario no puede ser nulo");
        }

        try {
            List<Tournament> tournaments = tournamentRepository.findByCreatorUserIdOrderByCreatedDateDesc(userId);
            return tournaments.stream()
                    .map(tournamentMapper::entityToDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Obtiene torneos disponibles para unirse
     *
     * @return lista de torneos disponibles
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public List<TournamentDTO> getAvailableTournaments() {
        try {
            List<Tournament> tournaments = tournamentRepository.findAvailableToJoin(TournamentStatus.WAITING_PLAYERS);
            return tournaments.stream()
                    .map(tournamentMapper::entityToDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Actualiza el estado de un torneo
     *
     * @param tournamentId ID del torneo
     * @param newStatus nuevo estado
     * @return torneo actualizado
     * @throws IllegalArgumentException si los parámetros son nulos
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    @Transactional
    public TournamentDTO updateTournamentStatus(Long tournamentId, TournamentStatus newStatus) {
        if (tournamentId == null || newStatus == null) {
            throw new IllegalArgumentException("El ID del torneo y el estado no pueden ser nulos");
        }

        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + tournamentId));

            tournament.setStatus(newStatus);
            Tournament updated = tournamentRepository.save(tournament);

            return tournamentMapper.entityToDTO(updated);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al actualizar el torneo en la base de datos", e);
        }
    }

    /**
     * Actualiza el número de jugadores actuales en un torneo
     *
     * @param tournamentId ID del torneo
     * @return torneo actualizado
     * @throws IllegalArgumentException si el tournamentId es nulo
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    @Transactional
    public TournamentDTO updateCurrentPlayers(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        }

        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + tournamentId));

            // Contar jugadores únicos en el torneo
            long currentPlayers = tournamentTeamRepository.countDistinctPlayersByTournamentId(tournamentId);
            tournament.setNumPlayers((int) currentPlayers); // Cambio aquí

            // Si se llenó el torneo de jugadores, cambiar estado
            if (currentPlayers >= tournament.getNumPlayers()
                    && // Cambio aquí
                    tournament.getStatus() == TournamentStatus.CREATED) {
                tournament.setStatus(TournamentStatus.WAITING_PLAYERS);
            }

            Tournament updated = tournamentRepository.save(tournament);
            return tournamentMapper.entityToDTO(updated);

        } catch (DataAccessException e) {
            throw new RuntimeException("Error al actualizar el torneo en la base de datos", e);
        }
    }

    /**
     * Elimina un torneo y todos sus equipos asociados
     *
     * @param tournamentId ID del torneo a eliminar
     * @throws IllegalArgumentException si el tournamentId es nulo
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    @Transactional
    public void deleteTournament(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        }

        try {
            if (!tournamentRepository.existsById(tournamentId)) {
                throw new RuntimeException("Torneo no encontrado con ID: " + tournamentId);
            }

            // Eliminar primero los equipos del torneo
            tournamentTeamRepository.deleteByTournamentId(tournamentId);

            // Luego eliminar el torneo
            tournamentRepository.deleteById(tournamentId);

        } catch (DataAccessException e) {
            throw new RuntimeException("Error al eliminar el torneo de la base de datos", e);
        }
    }
}

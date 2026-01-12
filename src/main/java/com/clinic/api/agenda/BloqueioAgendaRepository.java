// BloqueioAgendaRepository.java
package com.clinic.api.agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, UUID> {

    // Verifica se existe algum bloqueio que "Bate" com o horário que queremos agendar
    @Query("SELECT b FROM BloqueioAgenda b WHERE b.medico.id = :medicoId AND " +
            "((b.inicioBloqueio <= :fim AND b.fimBloqueio >= :inicio))")
    List<BloqueioAgenda> findBloqueiosNoIntervalo(@Param("medicoId") UUID medicoId,
                                                  @Param("inicio") LocalDateTime inicio,
                                                  @Param("fim") LocalDateTime fim);
}
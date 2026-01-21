package com.clinic.api.agenda.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, UUID> {

    @Query("SELECT b FROM BloqueioAgenda b WHERE b.medico.id = :medicoId AND " +
            "((b.inicioBloqueio <= :fim AND b.fimBloqueio >= :inicio))")
    List<BloqueioAgenda> findBloqueiosNoIntervalo(@Param("medicoId") UUID medicoId,
                                                  @Param("inicio") LocalDateTime inicio,
                                                  @Param("fim") LocalDateTime fim);
}
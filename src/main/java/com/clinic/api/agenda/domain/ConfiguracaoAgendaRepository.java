package com.clinic.api.agenda.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.DayOfWeek;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfiguracaoAgendaRepository extends JpaRepository<ConfiguracaoAgenda, UUID> {
    Optional<ConfiguracaoAgenda> findByMedicoIdAndDiaSemana(UUID medicoId, DayOfWeek diaSemana);
}
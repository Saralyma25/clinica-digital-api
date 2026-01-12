// ConfiguracaoAgendaRepository.java
package com.clinic.api.agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.DayOfWeek;
import java.util.Optional;
import java.util.UUID;

public interface ConfiguracaoAgendaRepository extends JpaRepository<ConfiguracaoAgenda, UUID> {
    // Busca a configuração de um médico para uma Terça-feira (por exemplo)
    Optional<ConfiguracaoAgenda> findByMedicoIdAndDiaSemana(UUID medicoId, DayOfWeek diaSemana);
}
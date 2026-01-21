package com.clinic.api.agendamento;

import com.clinic.api.medico.enun.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

    // Buscas padrão
    List<Agendamento> findByMedicoId(UUID medicoId);
    List<Agendamento> findByPacienteId(UUID pacienteId);

    // Busca para o Dashboard Diário (Ordenado por horário)
    List<Agendamento> findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(UUID medicoId, LocalDateTime start, LocalDateTime end);

    // Validações de Choque de Horário
    boolean existsByMedicoIdAndDataConsulta(UUID medicoId, LocalDateTime dataConsulta);

    // Trava para Paciente: não pode ter 2 consultas no mesmo horário (exceto se cancelado)
    boolean existsByPacienteIdAndDataConsultaAndStatusNot(UUID pacienteId, LocalDateTime data, String status);

    // Trava de Duplicidade Ativa: Paciente não pode ter 2 consultas 'ABERTAS' da mesma especialidade
    boolean existsByPacienteIdAndMedico_EspecialidadeAndStatusIn(UUID pacienteId, Especialidade especialidade, List<String> status);

    // Limpeza automática
    @Modifying
    @Query("DELETE FROM Agendamento a WHERE a.status = :status AND a.dataCadastro < :limite")
    void deleteByStatusAndDataCadastroBefore(@Param("status") String status, @Param("limite") LocalDateTime limite);
}
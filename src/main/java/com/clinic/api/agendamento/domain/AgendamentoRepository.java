package com.clinic.api.agendamento.domain;

import com.clinic.api.agendamento.Agendamento;
import com.clinic.api.agendamento.domain.StatusAgendamento;
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

    // Busca para o Dashboard Diário
    List<Agendamento> findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(UUID medicoId, LocalDateTime start, LocalDateTime end);

    // Validações de Choque de Horário (Simples)
    boolean existsByMedicoIdAndDataConsulta(UUID medicoId, LocalDateTime dataConsulta);

    // Validação Complexa 1: Paciente não pode estar em dois lugares ao mesmo tempo
    // Verifica se existe agendamento ativo (Status diferente de Cancelado)
    boolean existsByPacienteIdAndDataConsultaAndStatusNot(UUID pacienteId, LocalDateTime data, StatusAgendamento statusExcluido);

    // Validação Complexa 2: Regra de Negócio (Duplicidade de especialidade ativa)
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a WHERE a.paciente.id = :pacienteId " +
            "AND a.medico.especialidade = :especialidade " +
            "AND a.status IN :listaStatus")
    boolean existsByPacienteIdAndEspecialidadeAndStatus(
            @Param("pacienteId") UUID pacienteId,
            @Param("especialidade") Especialidade especialidade,
            @Param("listaStatus") List<StatusAgendamento> listaStatus);

    // Limpeza automática (Garbage Collector de agendamentos abandonados)
    @Modifying
    @Query("DELETE FROM Agendamento a WHERE a.status = :status AND a.dataCadastro < :limite")
    void deleteByStatusAndDataCadastroBefore(@Param("status") StatusAgendamento status, @Param("limite") LocalDateTime limite);
}
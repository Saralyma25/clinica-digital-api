package com.clinic.api.agendamento;

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

    // --- 1. RELATÓRIOS E LISTAGENS ---
    List<Agendamento> findByMedicoId(UUID medicoId);
    List<Agendamento> findByPacienteId(UUID pacienteId);
    List<Agendamento> findByMedico_EspecialidadeContainingIgnoreCase(String especialidade);
    List<Agendamento> findByDataConsulta(LocalDateTime dataConsulta);

    // Agendamentos dentro de um intervalo (Ex: Relatório do dia)
    List<Agendamento> findByDataConsultaBetween(LocalDateTime inicio, LocalDateTime fim);

    // --- 2. TRAVAS DE SEGURANÇA (Retornam True/False) ---

    // Verifica se já existe agendamento para o Médico X na Data Y
    boolean existsByMedicoIdAndDataConsulta(UUID medicoId, LocalDateTime dataConsulta);

    // Verifica se existe agendamento do Paciente para a mesma especialidade (Ignorando cancelados)
    boolean existsByPacienteIdAndMedico_EspecialidadeAndStatusNot(
            UUID pacienteId,
            String especialidade,
            String status
    );

    // --- 3. ROTINAS AUTOMÁTICAS (Robô) ---

    @Modifying // Indica que vai alterar o banco (Delete)
    @Query("DELETE FROM Agendamento a WHERE a.status = 'EM_PROCESSAMENTO' AND a.dataCadastro < :limite")
    void limparAgendamentosExpirados(@Param("limite") LocalDateTime limite);
}
package com.clinic.api.prontuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, UUID> {

    // Buscar pelo ID do agendamento
    Optional<Prontuario> findByAgendamentoId(UUID agendamentoId);

    // Buscar por Nome do Paciente (Navega: Prontuario -> Agendamento -> Paciente -> Nome)
    List<Prontuario> findByAgendamento_Paciente_NomeContainingIgnoreCase(String nomePaciente);

    // Buscar por CPF do Paciente
    List<Prontuario> findByAgendamento_Paciente_Cpf(String cpf);

    // Buscar por Médico
    List<Prontuario> findByAgendamento_MedicoId(UUID medicoId);

    // Buscar por Especialidade
    List<Prontuario> findByAgendamento_Medico_EspecialidadeContainingIgnoreCase(String especialidade);

    // Busca todas as folhas (consultas) da pasta desse paciente, ordenadas da mais recente para a mais antiga
    @Query("SELECT p FROM Prontuario p WHERE p.agendamento.paciente.id = :pacienteId ORDER BY p.agendamento.dataConsulta DESC")
    List<Prontuario> buscarHistoricoCompletoDoPaciente(@Param("pacienteId") UUID pacienteId);


    // Buscar por Data do Agendamento (Intervalo)
    List<Prontuario> findByAgendamento_DataConsultaBetween(LocalDateTime inicio, LocalDateTime fim);
}
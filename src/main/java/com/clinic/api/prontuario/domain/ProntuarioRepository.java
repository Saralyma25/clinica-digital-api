package com.clinic.api.prontuario.domain;

import com.clinic.api.prontuario.Prontuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, UUID> {

    Optional<Prontuario> findByAgendamentoId(UUID agendamentoId);

    @Query("SELECT p FROM Prontuario p WHERE p.agendamento.paciente.id = :pacienteId ORDER BY p.agendamento.dataConsulta DESC")
    List<Prontuario> buscarHistoricoCompletoDoPaciente(@Param("pacienteId") UUID pacienteId);
}
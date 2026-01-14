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

    // --- 1. BUSCAS E RELATÓRIOS ---
    List<Agendamento> findByMedicoId(UUID medicoId);
    List<Agendamento> findByPacienteId(UUID pacienteId);

    // CORREÇÃO: Removido o "ContainingIgnoreCase" que causava erro com Enum
    List<Agendamento> findByMedico_Especialidade(Especialidade especialidade);

    List<Agendamento> findByDataConsulta(LocalDateTime dataConsulta);
    List<Agendamento> findByDataConsultaBetween(LocalDateTime inicio, LocalDateTime fim);

    // --- 2. REGRAS DE NEGÓCIO E TRAVAS ---

    // Verifica se o médico já tem compromisso no horário
    boolean existsByMedicoIdAndDataConsulta(UUID medicoId, LocalDateTime dataConsulta);

    // Trava para evitar consultas duplicadas da mesma especialidade para o mesmo paciente
    boolean existsByPacienteIdAndMedico_EspecialidadeAndStatusIn(UUID pacienteId, Especialidade especialidade, List<String> status);

    // Evita choque de horários para o paciente
    boolean existsByPacienteIdAndDataConsultaAndStatusNot(UUID pacienteId, LocalDateTime data, String status);

    // Busca textual flexível via Query Manual (Seguro para Enums)
    @Query("SELECT a FROM Agendamento a WHERE UPPER(STR(a.medico.especialidade)) LIKE UPPER(CONCAT('%', :especialidade, '%'))")
    List<Agendamento> buscarPorEspecialidade(@Param("especialidade") String especialidade);

    // --- 3. OPERAÇÕES DE MANUTENÇÃO ---

    @Modifying
    @Query("DELETE FROM Agendamento a WHERE a.status = :status AND a.dataCadastro < :limite")
    void deleteByStatusAndDataCadastroBefore(@Param("status") String status, @Param("limite") LocalDateTime limite);

    @Modifying
    @Query("DELETE FROM Agendamento a WHERE a.status = 'EM_PROCESSAMENTO' AND a.dataCadastro < :limite")
    void limparAgendamentosExpirados(@Param("limite") LocalDateTime limite);

    // Método essencial para o Dashboard do Médico (Lista do Dia)
    List<Agendamento> findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(UUID medicoId, LocalDateTime start, LocalDateTime end);
}
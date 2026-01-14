package com.clinic.api.agendamento;

import com.clinic.api.medico.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

    // --- 1. BUSCAS E RELATÓRIOS ---
    List<Agendamento> findByMedicoId(UUID medicoId);
    List<Agendamento> findByPacienteId(UUID pacienteId);

    // Mantido para compatibilidade com buscas textuais
    List<Agendamento> findByMedico_EspecialidadeContainingIgnoreCase(String especialidade);

    // Adicionado: Busca por Enum para relatórios precisos
    List<Agendamento> findByMedico_Especialidade(Especialidade especialidade);

    List<Agendamento> findByDataConsulta(LocalDateTime dataConsulta);
    List<Agendamento> findByDataConsultaBetween(LocalDateTime inicio, LocalDateTime fim);

    // --- 2. REGRAS DE NEGÓCIO E TRAVAS (EXISTENCE CHECKS) ---

    // ACRESCENTADO: Verifica se o MÉDICO já tem compromisso (Essencial para o Service)
    boolean existsByMedicoIdAndDataConsulta(UUID medicoId, LocalDateTime dataConsulta);

    // REFORMULADO: Trava de especialidade usando o ENUM (Padrão novo do sistema)
   // boolean existsByPacienteIdAndMedico_EspecialidadeAndStatusIn(UUID pacienteId, String especialidade, List<String> status);
    // Antes estava String especialidade. Agora mudamos para Especialidade especialidade.
    boolean existsByPacienteIdAndMedico_EspecialidadeAndStatusIn(UUID pacienteId, Especialidade especialidade, List<String> status);


    // NOVA TRAVA: Evita que o PACIENTE agende dois médicos no mesmo horário
    boolean existsByPacienteIdAndDataConsultaAndStatusNot(UUID pacienteId, LocalDateTime data, String status);

    // --- 3. OPERAÇÕES DE LIMPEZA (FAXINEIRO 🤖) ---

    @Modifying
    @Query("DELETE FROM Agendamento a WHERE a.status = :status AND a.dataCadastro < :limite")
    void deleteByStatusAndDataCadastroBefore(@Param("status") String status, @Param("limite") LocalDateTime limite);

    @Modifying
    @Query("DELETE FROM Agendamento a WHERE a.status = 'EM_PROCESSAMENTO' AND a.dataCadastro < :limite")
    void limparAgendamentosExpirados(@Param("limite") LocalDateTime limite);

    // 3. --- A PEÇA QUE FALTAVA (Lista do Dia) ---
    // Este é o método que o seu Service está gritando que não encontra.
    // Ele busca por médico, num intervalo de tempo (inicio e fim do dia) e ordena por horário.
    List<Agendamento> findByMedicoIdAndDataConsultaBetweenOrderByDataConsultaAsc(UUID medicoId, LocalDateTime start, LocalDateTime end);
}
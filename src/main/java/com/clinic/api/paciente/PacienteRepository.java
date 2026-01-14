package com.clinic.api.paciente;

import com.clinic.api.medico.enun.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, UUID> {

    Optional<Paciente> findByCpf(String cpf);
    Optional<Paciente> findByEmail(String email);
    Optional<Paciente> findByTelefone(String telefone);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);

    // Busca por nome parcial
    List<Paciente> findByNomeContainingIgnoreCase(String nome);

    // Listar pacientes de um médico específico
    List<Paciente> findByMedicoId(UUID medicoId);

    // AGORA (Certo 1): Buscar pelo ID do PLANO (Ligação direta)
    List<Paciente> findByPlanoId(UUID planoId);

    // AGORA (Certo 2): Buscar pelo CONVÊNIO através do Plano
    // O underline (_) diz ao Spring: "Entre em Plano e pegue o ConvenioId dele"
    List<Paciente> findByPlano_ConvenioId(UUID convenioId);

    // Listar pacientes por especialidade do médico vinculado
    List<Paciente> findByMedico_Especialidade(Especialidade especialidade);
}


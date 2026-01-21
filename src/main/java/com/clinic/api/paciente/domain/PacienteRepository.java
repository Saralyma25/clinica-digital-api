package com.clinic.api.paciente.domain;


import com.clinic.api.medico.enun.Especialidade;
import com.clinic.api.paciente.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, UUID> {

    Optional<Paciente> findByCpf(String cpf);

    // AJUSTE: Agora buscamos o email que está dentro da entidade Usuario
    Optional<Paciente> findByUsuarioEmail(String email);

    Optional<Paciente> findByTelefone(String telefone);

    boolean existsByCpf(String cpf);

    // AJUSTE: Verifica existência pelo email do Usuario vinculado
    boolean existsByUsuarioEmail(String email);

    // Busca por nome parcial
    List<Paciente> findByNomeContainingIgnoreCase(String nome);

    // Listar pacientes de um médico específico
    List<Paciente> findByMedicoId(UUID medicoId);

    // Buscar pelo ID do PLANO
    List<Paciente> findByPlanoId(UUID planoId);

    // Buscar pelo CONVÊNIO através do Plano (usando a navegação de propriedade _)
    List<Paciente> findByPlano_ConvenioId(UUID convenioId);

    // Listar pacientes por especialidade do médico vinculado
    List<Paciente> findByMedico_Especialidade(Especialidade especialidade);
}
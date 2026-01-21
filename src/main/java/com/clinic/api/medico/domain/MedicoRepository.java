package com.clinic.api.medico.domain;

import com.clinic.api.medico.Medico;
import com.clinic.api.medico.enun.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, UUID> {

    /**
     * Busca o médico através do e-mail do usuário vinculado.
     * O Spring JPA entende que deve ir na entidade 'Usuario' e buscar o campo 'email'.
     */
    Optional<Medico> findByUsuarioEmail(String email);

    /**
     * Busca um médico pelo seu número de CRM.
     */
    Optional<Medico> findByCrm(String crm);

    /**
     * Lista todos os médicos de uma determinada especialidade.
     */
    List<Medico> findByEspecialidade(Especialidade especialidade);

    /**
     * Busca médicos pelo nome (parcial e ignorando maiúsculas/minúsculas).
     */
    List<Medico> findByNomeContainingIgnoreCase(String nome);

    /**
     * Verifica se já existe um médico com o CRM informado.
     */
    boolean existsByCrm(String crm);
}
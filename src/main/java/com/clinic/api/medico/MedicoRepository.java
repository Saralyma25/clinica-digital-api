package com.clinic.api.medico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, UUID> {

    Optional<Medico> findByEmail(String email);
    Optional<Medico> findByCrm(String crm);

    List<Medico> findByNomeContainingIgnoreCase(String nome);

    // Agora busca pelo tipo exato do Enum
    List<Medico> findByEspecialidade(Especialidade especialidade);
}
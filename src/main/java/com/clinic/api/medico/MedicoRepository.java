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

    // Busca por parte do nome
    List<Medico> findByNomeContainingIgnoreCase(String nome);

    // Busca por especialidade (Ex: "Cardio" acha "Cardiologista")
    List<Medico> findByEspecialidadeContainingIgnoreCase(String especialidade);
}
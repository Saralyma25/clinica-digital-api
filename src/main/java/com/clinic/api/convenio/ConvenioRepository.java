package com.clinic.api.convenio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConvenioRepository extends JpaRepository<Convenio, UUID> {

    // Busca por nome (Ex: "Brad" acha "Bradesco")
    List<Convenio> findByNomeContainingIgnoreCase(String nome);
}
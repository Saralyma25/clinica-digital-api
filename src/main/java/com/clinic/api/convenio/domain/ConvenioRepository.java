package com.clinic.api.convenio.domain;

import com.clinic.api.convenio.Convenio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConvenioRepository extends JpaRepository<Convenio, UUID> {

    // Validações exatas (para evitar duplicidade)
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByRegistroAns(String registroAns);

    // Busca para o usuário (Autocomplete)
    List<Convenio> findByNomeContainingIgnoreCase(String nome);
}
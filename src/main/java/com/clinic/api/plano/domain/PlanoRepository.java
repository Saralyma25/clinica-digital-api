package com.clinic.api.plano.domain;

import com.clinic.api.plano.Plano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlanoRepository extends JpaRepository<Plano, UUID> {

    // Busca Planos ativos de um convênio específico (Para o Select do Front-end)
    List<Plano> findByConvenioIdAndAtivoTrue(UUID convenioId);

    // Busca genérica para admins
    List<Plano> findByConvenioId(UUID convenioId);
}
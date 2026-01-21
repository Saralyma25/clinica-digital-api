package com.clinic.api.prontuario.domain;

import com.clinic.api.prontuario.DadosClinicosFixos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DadosClinicosFixosRepository extends JpaRepository<DadosClinicosFixos, UUID> {
}
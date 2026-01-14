package com.clinic.api.prontuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DadosClinicosFixosRepository extends JpaRepository<DadosClinicosFixos, UUID> {
    // Como o ID da tabela é o mesmo ID do Paciente (MapsId),
    // o findById padrão do JpaRepository já resolve nossa busca por paciente.
}
package com.clinic.api.documento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, UUID> {
    List<Documento> findByPacienteIdOrderByDataUploadDesc(UUID pacienteId);

    // Essencial para o Alerta do Dashboard
    long countByVistoPeloMedicoFalseAndOrigem(String origem);
}
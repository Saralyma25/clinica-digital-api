package com.clinic.api.documento.domain;

import com.clinic.api.documento.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, UUID> {

    // Lista documentos do paciente (mais recentes primeiro)
    List<Documento> findByPacienteIdOrderByDataUploadDesc(UUID pacienteId);

    // Dashboard: Conta quantos documentos enviados por pacientes o médico ainda não viu
    long countByVistoPeloMedicoFalseAndOrigem(String origem);
}
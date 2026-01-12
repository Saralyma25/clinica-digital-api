package com.clinic.api.clinica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicaRepository extends JpaRepository<Clinica, UUID> {

    // Busca exata pelo CNPJ
    Optional<Clinica> findByCnpj(String cnpj);

    // Busca por parte do nome (Ex: digita "Vida" e acha "Clínica Vida Saudável")
    List<Clinica> findByNomeFantasiaContainingIgnoreCase(String nomeFantasia);

    // Busca por parte do endereço
    List<Clinica> findByEnderecoContainingIgnoreCase(String endereco);
}
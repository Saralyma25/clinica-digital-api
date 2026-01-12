package com.clinic.api.clinica;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class ClinicaService {

    private final ClinicaRepository repository;

    public ClinicaService(ClinicaRepository repository) {
        this.repository = repository;
    }

    public Clinica salvar(Clinica clinica) {
        // Regra: Validar se já existe CNPJ igual
        if (repository.findByCnpj(clinica.getCnpj()).isPresent()) {
            throw new RuntimeException("Já existe uma clínica com este CNPJ.");
        }
        return repository.save(clinica);
    }

    public List<Clinica> listar() {
        return repository.findAll();
    }

    // Atualizar dados da clínica
    public Clinica atualizar(UUID id, Clinica novosDados) {
        Clinica clinica = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clínica não encontrada"));

        clinica.setNomeFantasia(novosDados.getNomeFantasia());
        clinica.setEndereco(novosDados.getEndereco());
        clinica.setTelefone(novosDados.getTelefone());

        return repository.save(clinica);
    }
}
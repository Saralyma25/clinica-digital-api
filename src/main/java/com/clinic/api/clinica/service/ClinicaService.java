package com.clinic.api.clinica.service;


import com.clinic.api.clinica.Clinica;
import com.clinic.api.clinica.domain.ClinicaRepository;
import com.clinic.api.clinica.dto.ClinicaRequest;
import com.clinic.api.clinica.dto.ClinicaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClinicaService {

    private final ClinicaRepository repository;

    public ClinicaService(ClinicaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ClinicaResponse salvar(ClinicaRequest request) {
        // Validação de unicidade
        if (repository.existsByCnpj(request.getCnpj())) {
            throw new RuntimeException("Já existe uma clínica cadastrada com este CNPJ.");
        }

        Clinica clinica = new Clinica(
                request.getRazaoSocial(),
                request.getNomeFantasia(),
                request.getCnpj(),
                request.getEndereco(),
                request.getTelefone()
        );

        return new ClinicaResponse(repository.save(clinica));
    }

    public List<ClinicaResponse> listar() {
        return repository.findAll().stream()
                .filter(Clinica::getAtivo) // Filtra apenas as ativas
                .map(ClinicaResponse::new)
                .collect(Collectors.toList());
    }

    public ClinicaResponse buscarPorId(UUID id) {
        Clinica clinica = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clínica não encontrada"));
        return new ClinicaResponse(clinica);
    }

    @Transactional
    public ClinicaResponse atualizar(UUID id, ClinicaRequest request) {
        Clinica clinica = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clínica não encontrada"));

        clinica.setNomeFantasia(request.getNomeFantasia());
        clinica.setRazaoSocial(request.getRazaoSocial());
        clinica.setEndereco(request.getEndereco());
        clinica.setTelefone(request.getTelefone());
        // CNPJ geralmente não se altera, mas se precisar, validar duplicidade aqui

        return new ClinicaResponse(repository.save(clinica));
    }

    @Transactional
    public void excluir(UUID id) {
        Clinica clinica = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clínica não encontrada"));

        clinica.setAtivo(false); // Soft Delete
        repository.save(clinica);
    }
}
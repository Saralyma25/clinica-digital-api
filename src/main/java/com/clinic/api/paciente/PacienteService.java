package com.clinic.api.paciente;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PacienteService {

    private final PacienteRepository repository;

    public PacienteService(PacienteRepository repository) {
        this.repository = repository;
    }

    // --- CREATE ---
    public Paciente cadastrar(Paciente paciente) {
        if (repository.findByCpf(paciente.getCpf()).isPresent()) {
            throw new RuntimeException("CPF já cadastrado.");
        }
        return repository.save(paciente);
    }

    // --- READ ---
    public List<Paciente> listarTodos() {
        return repository.findAll();
    }

    public Paciente buscarPorId(UUID id) {
        Optional<Paciente> paciente = repository.findById(id);
        if (paciente.isEmpty()) {
            throw new RuntimeException("Paciente não encontrado.");
        }
        return paciente.get();
    }

    // --- UPDATE ---
    public Paciente atualizar(UUID id, Paciente pacienteNovosDados) {
        Paciente pacienteExistente = buscarPorId(id);

        pacienteExistente.setNome(pacienteNovosDados.getNome());
        pacienteExistente.setTelefone(pacienteNovosDados.getTelefone());
        pacienteExistente.setEmail(pacienteNovosDados.getEmail());
        // Atualizar plano também, se necessário

        return repository.save(pacienteExistente);
    }

    // --- DELETE ---
    public void excluir(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Paciente não encontrado.");
        }
        repository.deleteById(id);
    }
}
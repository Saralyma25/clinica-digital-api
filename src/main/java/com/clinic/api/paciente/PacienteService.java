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
    // Local: com.clinic.api.paciente.PacienteService.java

    public Paciente cadastrar(Paciente paciente) {
        // 1. TRAVA: CPF (Documento obrigatório e único)
        if (repository.findByCpf(paciente.getCpf()).isPresent()) {
            throw new RuntimeException("Este CPF já está cadastrado no sistema.");
        }

        // 2. TRAVA: E-mail
        if (repository.findByEmail(paciente.getEmail()).isPresent()) {
            throw new RuntimeException("Este e-mail já está vinculado a outro paciente.");
        }

        // 3. TRAVA: Telefone (Opcional, mas recomendado para evitar cadastros duplicados)
        if (repository.findByTelefone(paciente.getTelefone()).isPresent()) {
            throw new RuntimeException("Este número de telefone já pertence a um cadastro existente.");
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
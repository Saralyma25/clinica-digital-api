package com.clinic.api.medico;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MedicoService {

    private final MedicoRepository repository;

    public MedicoService(MedicoRepository repository) {
        this.repository = repository;
    }

    // --- C: CREATE (Cadastrar) ---
    // Local: com.clinic.api.medico.MedicoService.java

    public Medico cadastrar(Medico medico) {
        // 1. TRAVA: CRM (Não pode repetir nunca)
        if (repository.findByCrm(medico.getCrm()).isPresent()) {
            throw new RuntimeException("Já existe um médico cadastrado com este CRM.");
        }

        // 2. TRAVA: E-mail (Identificador único de login)
        if (repository.findByEmail(medico.getEmail()).isPresent()) {
            throw new RuntimeException("Este e-mail já está em uso por outro profissional.");
        }

        // Nota: Removi propositalmente a trava de Nome para permitir homônimos.

        return repository.save(medico);
    }

    // --- R: READ (Listar e Buscar) ---
    public List<Medico> listarTodos() {
        return repository.findAll();
    }

    public Medico buscarPorId(UUID id) {
        Optional<Medico> medico = repository.findById(id);
        if (medico.isEmpty()) {
            throw new RuntimeException("Médico não encontrado.");
        }
        return medico.get();
    }


    // --- U: UPDATE (Atualizar) ---
    public Medico atualizar(UUID id, Medico medicoAtualizado) {
        // 1. Verifica se o médico existe no banco
        Medico medicoExistente = buscarPorId(id);

        // 2. Atualiza os dados básicos
        medicoExistente.setNome(medicoAtualizado.getNome());
        medicoExistente.setEspecialidade(medicoAtualizado.getEspecialidade());
        medicoExistente.setValorConsulta(medicoAtualizado.getValorConsulta());

        // 3. NOVOS CAMPOS: Atualiza a configuração da agenda (Dia 06)
        medicoExistente.setHoraInicio(medicoAtualizado.getHoraInicio());
        medicoExistente.setHoraFim(medicoAtualizado.getHoraFim());
        medicoExistente.setDuracaoConsulta(medicoAtualizado.getDuracaoConsulta());

        // 4. O .save() vê que tem ID e faz o UPDATE no banco
        return repository.save(medicoExistente);
    }

    // --- D: DELETE (Excluir) ---
    public void excluir(UUID id) {
        // Verifica se existe antes de tentar apagar
        if (!repository.existsById(id)) {
            throw new RuntimeException("Médico não encontrado para exclusão.");
        }
        repository.deleteById(id);
    }

    // Busca por Nome
    public List<Medico> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }
}
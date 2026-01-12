package com.clinic.api.prontuario;

import com.clinic.api.agendamento.AgendamentoRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.Optional;

@Service
public class ProntuarioService {

    private final ProntuarioRepository repository;
    private final AgendamentoRepository agendamentoRepository;

    public ProntuarioService(ProntuarioRepository repository, AgendamentoRepository agendamentoRepository) {
        this.repository = repository;
        this.agendamentoRepository = agendamentoRepository;
    }

    public Prontuario salvar(Prontuario prontuario) {
        // Regra: O Agendamento existe?
        if (prontuario.getAgendamento() == null ||
                !agendamentoRepository.existsById(prontuario.getAgendamento().getId())) {
            throw new RuntimeException("Agendamento inválido. O prontuário deve ser vinculado a uma consulta.");
        }

        // Regra: Já existe prontuário para essa consulta? (1 pra 1)
        Optional<Prontuario> existente = repository.findByAgendamentoId(prontuario.getAgendamento().getId());
        if (existente.isPresent() && !existente.get().getId().equals(prontuario.getId())) {
            throw new RuntimeException("Já existe um prontuário para este agendamento.");
        }

        return repository.save(prontuario);
    }

    public Prontuario buscarPorAgendamento(UUID agendamentoId) {
        return repository.findByAgendamentoId(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Prontuário não encontrado para este agendamento."));
    }
}
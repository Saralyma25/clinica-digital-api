package com.clinic.api.prontuario;

import com.clinic.api.agendamento.AgendamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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

//    public Prontuario salvar(Prontuario prontuario) {
//        // Regra: O Agendamento existe?
//        if (prontuario.getAgendamento() == null ||
//                !agendamentoRepository.existsById(prontuario.getAgendamento().getId())) {
//            throw new RuntimeException("Agendamento inválido. O prontuário deve ser vinculado a uma consulta.");
//        }
//
//        // Regra: Já existe prontuário para essa consulta? (1 pra 1)
//        Optional<Prontuario> existente = repository.findByAgendamentoId(prontuario.getAgendamento().getId());
//        if (existente.isPresent() && !existente.get().getId().equals(prontuario.getId())) {
//            throw new RuntimeException("Já existe um prontuário para este agendamento.");
//        }
//
//        return repository.save(prontuario);
//    }

//    public Prontuario buscarPorAgendamento(UUID agendamentoId) {
//        return repository.findByAgendamentoId(agendamentoId)
//                .orElseThrow(() -> new RuntimeException("Prontuário não encontrado para este agendamento."));
//    }

    // --- SALVAR (Cria uma nova folha na pasta) ---
    public Prontuario salvar(Prontuario prontuario) {
        // 1. Valida se o Agendamento existe
        if (prontuario.getAgendamento() == null ||
                !agendamentoRepository.existsById(prontuario.getAgendamento().getId())) {
            throw new RuntimeException("Agendamento inválido. O prontuário deve ser vinculado a uma consulta real.");
        }

        // 2. Trava de Unicidade: Uma consulta não pode ter dois prontuários
        // Isso protege contra erros de sistema (clicar duas vezes no botão salvar)
        Optional<Prontuario> existente = repository.findByAgendamentoId(prontuario.getAgendamento().getId());

        // Se já existe E não é o mesmo que estamos editando agora... ERRO.
        if (existente.isPresent() && !existente.get().getId().equals(prontuario.getId())) {
            throw new RuntimeException("Já existe um prontuário registrado para este agendamento (ID: " +
                    prontuario.getAgendamento().getId() + "). Edite o existente.");
        }

        return repository.save(prontuario);
    }

    // --- BUSCAR UM ESPECÍFICO (Ler a folha de hoje) ---
    public Prontuario buscarPorAgendamento(UUID agendamentoId) {
        return repository.findByAgendamentoId(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Prontuário não encontrado para este agendamento."));
    }

    // --- BUSCAR TUDO (Ler a Pasta Completa do Paciente) 📂 ---
    public List<Prontuario> listarHistoricoPaciente(UUID pacienteId) {
        // Retorna a lista cronológica (do mais recente para o mais antigo)
        return repository.buscarHistoricoCompletoDoPaciente(pacienteId);
    }

    // Método extra para buscar por ID do prontuário mesmo (caso precise editar)
    public Prontuario buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prontuário não encontrado."));
    }
}
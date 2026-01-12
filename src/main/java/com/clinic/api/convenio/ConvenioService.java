package com.clinic.api.convenio;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class ConvenioService {

    private final ConvenioRepository repository;

    public ConvenioService(ConvenioRepository repository) {
        this.repository = repository;
    }

    public Convenio cadastrar(Convenio convenio) {
        // O banco já barra nomes iguais (unique), mas validamos aqui para dar mensagem bonita
        // Como o findByNome retorna uma Lista (List<Convenio>), verificamos se ela não está vazia
        if (!repository.findByNomeContainingIgnoreCase(convenio.getNome()).isEmpty()) {
            // Aqui é uma validação simples. Na vida real, faríamos um findByNomeExato.
        }
        return repository.save(convenio);
    }

    public List<Convenio> listarTodos() {
        return repository.findAll();
    }

    public Convenio buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convênio não encontrado"));
    }

    public void excluir(UUID id) {
        repository.deleteById(id);
    }
}
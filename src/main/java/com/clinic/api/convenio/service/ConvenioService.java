package com.clinic.api.convenio.service;

import com.clinic.api.convenio.Convenio;
import com.clinic.api.convenio.domain.ConvenioRepository;
import com.clinic.api.convenio.dto.ConvenioRequest;
import com.clinic.api.convenio.dto.ConvenioResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConvenioService {

    private final ConvenioRepository repository;

    public ConvenioService(ConvenioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ConvenioResponse cadastrar(ConvenioRequest request) {
        if (repository.existsByNomeIgnoreCase(request.getNome())) {
            throw new RuntimeException("Já existe um convênio com o nome: " + request.getNome());
        }
        if (repository.existsByRegistroAns(request.getRegistroAns())) {
            throw new RuntimeException("Já existe um convênio com este registro ANS.");
        }

        Convenio convenio = new Convenio(
                request.getNome(),
                request.getRegistroAns(),
                request.getDiasParaPagamento()
        );

        return new ConvenioResponse(repository.save(convenio));
    }

    public List<ConvenioResponse> listarTodos() {
        return repository.findAll().stream()
                .filter(Convenio::getAtivo)
                .map(ConvenioResponse::new)
                .collect(Collectors.toList());
    }

    public ConvenioResponse buscarPorId(UUID id) {
        Convenio convenio = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convênio não encontrado"));
        return new ConvenioResponse(convenio);
    }

    public List<ConvenioResponse> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome).stream()
                .filter(Convenio::getAtivo)
                .map(ConvenioResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConvenioResponse atualizar(UUID id, ConvenioRequest request) {
        Convenio convenio = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convênio não encontrado para atualização"));

        convenio.setNome(request.getNome());
        convenio.setRegistroAns(request.getRegistroAns());

        // AGORA VAI FUNCIONAR (pois adicionamos o Setter na Entidade)
        convenio.setDiasPagamento(request.getDiasParaPagamento());

        return new ConvenioResponse(repository.save(convenio));
    }

    @Transactional
    public void excluir(UUID id) {
        Convenio convenio = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convênio não encontrado"));

        convenio.setAtivo(false);
        repository.save(convenio);
    }
}
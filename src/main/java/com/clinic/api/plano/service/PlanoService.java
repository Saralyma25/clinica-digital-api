package com.clinic.api.plano.service;

import com.clinic.api.convenio.Convenio;
import com.clinic.api.convenio.domain.ConvenioRepository;
import com.clinic.api.plano.Plano;
import com.clinic.api.plano.domain.PlanoRepository;
import com.clinic.api.plano.dto.PlanoRequest;
import com.clinic.api.plano.dto.PlanoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlanoService {

    private final PlanoRepository repository;
    private final ConvenioRepository convenioRepository;

    public PlanoService(PlanoRepository repository, ConvenioRepository convenioRepository) {
        this.repository = repository;
        this.convenioRepository = convenioRepository;
    }

    @Transactional
    public PlanoResponse cadastrar(PlanoRequest request) {
        // 1. Busca o Pai (Convenio)
        Convenio convenio = convenioRepository.findById(request.getConvenioId())
                .orElseThrow(() -> new RuntimeException("Convênio não encontrado com o ID informado."));

        // 2. Cria o Filho (Plano)
        Plano plano = new Plano(request.getNome(), request.getValorRepasse(), convenio);

        // 3. Salva
        return new PlanoResponse(repository.save(plano));
    }

    public List<PlanoResponse> listarTodos() {
        return repository.findAll().stream()
                .filter(Plano::getAtivo)
                .map(PlanoResponse::new)
                .collect(Collectors.toList());
    }

    // Método crucial para o Front-end: "Carregar planos deste convênio"
    public List<PlanoResponse> listarPorConvenio(UUID convenioId) {
        return repository.findByConvenioIdAndAtivoTrue(convenioId).stream()
                .map(PlanoResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void excluir(UUID id) {
        Plano plano = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));
        plano.setAtivo(false);
        repository.save(plano);
    }
}
package com.clinic.api.plano;

import com.clinic.api.convenio.ConvenioRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class PlanoService {

    private final PlanoRepository repository;
    private final ConvenioRepository convenioRepository; // Precisamos validar o pai

    public PlanoService(PlanoRepository repository, ConvenioRepository convenioRepository) {
        this.repository = repository;
        this.convenioRepository = convenioRepository;
    }

    public Plano cadastrar(Plano plano) {
        // Validação: O convênio informado existe?
        if (plano.getConvenio() == null || !convenioRepository.existsById(plano.getConvenio().getId())) {
            throw new RuntimeException("Convênio inválido ou não informado.");
        }
        return repository.save(plano);
    }

    public List<Plano> listarTodos() {
        return repository.findAll();
    }

    // Buscar planos de um convênio específico (Útil para a tela de seleção!)
    public List<Plano> listarPorConvenio(UUID convenioId) {
        return repository.findByConvenioId(convenioId);
    }
}
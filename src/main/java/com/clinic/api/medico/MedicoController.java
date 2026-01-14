package com.clinic.api.medico;

import com.clinic.api.medico.dto.MedicoRequest;
import com.clinic.api.medico.dto.MedicoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoService service;

    public MedicoController(MedicoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MedicoResponse> cadastrar(@RequestBody @Valid MedicoRequest request) {

        // 1. Converter DTO -> Entidade
        Medico medico = new Medico();
        medico.setNome(request.getNome());
        medico.setCrm(request.getCrm());
        medico.setEmail(request.getEmail());

        // CORREÇÃO: O Medico agora espera Especialidade (Enum).
        // Certifique-se de que request.getEspecialidade() também retorne o Enum Especialidade.
        medico.setEspecialidade(request.getEspecialidade());

        medico.setValorConsulta(request.getValorConsulta());
        medico.setSenha("123456");

        // 2. Chamar o Service
        Medico medicoSalvo = service.cadastrar(medico);

        // 3. Converter Entidade -> DTO de Resposta
        return ResponseEntity.status(HttpStatus.CREATED).body(new MedicoResponse(medicoSalvo));
    }

    @GetMapping
    public ResponseEntity<List<MedicoResponse>> listar() {
        List<MedicoResponse> lista = service.listarTodos().stream()
                .map(MedicoResponse::new)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medico> atualizar(@PathVariable UUID id, @RequestBody Medico dadosNovos) {
        Medico medicoAtualizado = service.atualizar(id, dadosNovos);
        return ResponseEntity.ok(medicoAtualizado);
    }
}
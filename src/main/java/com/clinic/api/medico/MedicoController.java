package com.clinic.api.medico;

import com.clinic.api.medico.dto.MedicoRequest;
import com.clinic.api.medico.dto.MedicoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController // Diz ao Spring: "Isso aqui responde Web"
@RequestMapping("/medicos") // O endereço base: http://localhost:8080/medicos
public class MedicoController {

    private final MedicoService service;

    public MedicoController(MedicoService service) {
        this.service = service;
    }

    @PostMapping // Verbo POST (Criar)
    // @Valid ativa as anotações do DTO (@NotBlank, etc)
    public ResponseEntity<MedicoResponse> cadastrar(@RequestBody @Valid MedicoRequest request) {

        // 1. Converter DTO -> Entidade
        Medico medico = new Medico();
        medico.setNome(request.getNome());
        medico.setCrm(request.getCrm());
        medico.setEmail(request.getEmail());
        medico.setEspecialidade(request.getEspecialidade());
        medico.setValorConsulta(request.getValorConsulta());
        medico.setSenha("123456");

        // 2. Chamar o Service
        Medico medicoSalvo = service.cadastrar(medico);

        // 3. Converter Entidade -> DTO de Resposta
        return ResponseEntity.status(HttpStatus.CREATED).body(new MedicoResponse(medicoSalvo));
    }

    @GetMapping // Verbo GET (Listar)
    public ResponseEntity<List<MedicoResponse>> listar() {
        // Busca todos e converte cada um para DTO usando stream (Java 8+)
        List<MedicoResponse> lista = service.listarTodos().stream()
                .map(MedicoResponse::new) // Chama o construtor do DTO
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medico> atualizar(@PathVariable UUID id, @RequestBody Medico dadosNovos) {
        // Chama o service, que agora sabe como atualizar
        Medico medicoAtualizado = service.atualizar(id, dadosNovos);

        return ResponseEntity.ok(medicoAtualizado);
    }

}
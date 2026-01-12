package com.clinic.api.paciente;

import com.clinic.api.paciente.dto.PacienteRequest;
import com.clinic.api.paciente.dto.PacienteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> cadastrar(@RequestBody @Valid PacienteRequest request) {
        Paciente paciente = new Paciente();
        paciente.setNome(request.getNome());
        paciente.setEmail(request.getEmail());
        paciente.setTelefone(request.getTelefone());
        paciente.setCpf(request.getCpf());

        // Importante: Definindo senha padrão para evitar erro 500 no banco
        // Futuramente isso virá de um campo no cadastro
        // paciente.setSenha("paciente123");

        Paciente pacienteSalvo = service.cadastrar(paciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PacienteResponse(pacienteSalvo));
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listar() {
        List<PacienteResponse> lista = service.listarTodos().stream()
                .map(PacienteResponse::new)
                .toList();
        return ResponseEntity.ok(lista);
    }
}
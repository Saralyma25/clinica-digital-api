package com.clinic.api.paciente;

import com.clinic.api.paciente.dto.DadosCadastroBasico;
import com.clinic.api.paciente.dto.PacienteRequest; // Esse continua existindo para o cadastro completo
import com.clinic.api.paciente.dto.PacienteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    // CENÁRIO 1: O usuário chegou pelo Google (Só temos Nome e Email)
    @PostMapping("/rapido")
    public ResponseEntity<PacienteResponse> cadastrarRapido(@RequestBody @Valid PacienteBasicoRequest request) {
        Paciente paciente = new Paciente();
        paciente.setNome(request.nome());
        paciente.setEmail(request.email());
        paciente.setCadastroCompleto(false); // <--- Marcamos que falta CPF

        // O Service salva (agora que a Entidade Paciente aceita CPF null)
        Paciente salvo = service.cadastrar(paciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PacienteResponse(salvo));
    }

    // CENÁRIO 2: O usuário preencheu o formulário completo (Seu método antigo)
    @PostMapping
    public ResponseEntity<PacienteResponse> cadastrarCompleto(@RequestBody @Valid PacienteRequest request) {
        Paciente paciente = new Paciente();
        // Copia todos os dados (CPF, Tel, etc)
        BeanUtils.copyProperties(request, paciente); 
        paciente.setCadastroCompleto(true); // <--- Cadastro VIP, tudo certo!

        Paciente salvo = service.cadastrar(paciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PacienteResponse(salvo));
    }
    // TODO: Criar endpoint @PutMapping("/{id}") para completar o cadastro depois

    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listar() {
        List<PacienteResponse> lista = service.listarTodos().stream()
                .map(PacienteResponse::new)
                .toList();
        return ResponseEntity.ok(lista);
    }
}
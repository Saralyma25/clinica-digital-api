package com.clinic.api.plano.controller;

import com.clinic.api.plano.dto.PlanoRequest;
import com.clinic.api.plano.dto.PlanoResponse;
import com.clinic.api.plano.service.PlanoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/planos")
public class PlanoController {

    private final PlanoService service;

    public PlanoController(PlanoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PlanoResponse> cadastrar(@RequestBody @Valid PlanoRequest request) {
        var response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PlanoResponse>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    // Endpoint Específico: Traz os planos de UM convênio
    // Ex: GET /planos/convenio/UUID-DA-UNIMED -> Retorna [Flex, Top, Basico]
    @GetMapping("/convenio/{convenioId}")
    public ResponseEntity<List<PlanoResponse>> listarPorConvenio(@PathVariable UUID convenioId) {
        return ResponseEntity.ok(service.listarPorConvenio(convenioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
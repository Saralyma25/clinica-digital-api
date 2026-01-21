//package com.clinic.api.usuario;
//
//import com.clinic.api.usuario.dto.DadosAutenticacao;
//import com.clinic.api.usuario.dto.DadosTokenJWT;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/login")
//public class AutenticacaoController {
//
//    private final AuthenticationManager manager;
//    private final TokenService tokenService;
//
//    @Autowired
//    public AutenticacaoController(AuthenticationManager manager, TokenService tokenService) {
//        this.manager = manager;
//        this.tokenService = tokenService;
//    }
//
//    @PostMapping
//    public ResponseEntity efetuarLogin(@RequestBody @Valid DadosAutenticacao dados) {
//        // Cria o token de autenticação do Spring com e-mail e senha
//        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());
//
//        // O manager chama o AutenticacaoService para validar no banco
//        var authentication = manager.authenticate(authenticationToken);
//
//        // Se validado, gera o crachá digital (JWT)
//        var tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());
//
//        return ResponseEntity.ok(new DadosTokenJWT(tokenJWT));
//    }
//}
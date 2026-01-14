package com.clinic.api.usuario;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    // A senha secreta deve estar no seu application.properties
    @Value("${api.security.token.secret}")
    private String secret;

    private static final String ISSUER = "API Clinica Digital";

    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(usuario.getEmail()) // Identificamos o dono do token pelo e-mail
                    .withClaim("id", usuario.getId().toString()) // Adicionamos o ID como informação extra
                    .withClaim("role", usuario.getRole().name()) // Adicionamos a Role (MEDICO, RECEPCAO, etc)
                    .withExpiresAt(dataExpiracao()) // O token expira em 2 horas
                    .sign(algoritmo);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token jwt", exception);
        }
    }

    public String getSubject(String tokenJWT) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            return JWT.require(algoritmo)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token JWT inválido ou expirado!");
        }
    }

    private Instant dataExpiracao() {
        // Define que o token vale por 2 horas a partir de agora (Horário de Brasília)
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
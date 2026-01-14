package com.clinic.api.infra.security;

import com.clinic.api.usuario.UsuarioRepository;
import com.clinic.api.usuario.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository repository;

    @Autowired
    public SecurityFilter(TokenService tokenService, UsuarioRepository repository) {
        this.tokenService = tokenService;
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Tenta recuperar o token do cabeçalho "Authorization"
        String tokenJWT = recuperarToken(request);

        if (tokenJWT != null) {
            // 2. Valida o token e pega o e-mail do usuário (subject)
            String subject = tokenService.getSubject(tokenJWT);

            // 3. Busca o usuário no banco de dados para confirmar que ele ainda existe
            UserDetails usuario = repository.findByEmail(subject);

            // 4. Cria o objeto de autenticação que o Spring Security entende
            var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

            // 5. "Loga" o usuário no contexto do Spring para esta requisição específica
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 6. Continua o fluxo da requisição para o próximo filtro ou para o Controller
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.replace("Bearer ", "").trim();
        }

        return null;
    }
}
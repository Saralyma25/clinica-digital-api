package com.clinic.api.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    // Injetamos o filtro que criamos para validar o Token em cada requisição
    private final SecurityFilter securityFilter;

    @Autowired
    public SecurityConfigurations(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        return http
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(req -> {
//                    // 1. Libera o Login (essencial para o médico conseguir o crachá)
//                    req.requestMatchers(HttpMethod.POST, "/login").permitAll();
//
//                    // 2. Mantém a liberação de cadastro de médicos (útil para testes iniciais)
//                    req.requestMatchers(HttpMethod.POST, "/medicos").permitAll();
//
//                    // 3. TODO: [DIA 11] Refinar níveis de acesso detalhados (Permissions).
//                    // RECEPCAO: Agenda, Faturamento, Cadastro.
//                    // ENFERMAGEM: Triagem, Curativos.
//                    // STAFF (Vigia): Apenas consulta de agenda de entradas.
//
//                    // 4. BLOQUEIO: Qualquer outra requisição agora exige estar logado
//                    req.anyRequest().authenticated();
//                })
//                // ADICIONADO: Diz ao Spring para rodar nosso filtro JWT ANTES do filtro de login padrão
//                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
//                .build();
//    }

    // NOVO MÉTODO: Necessário para o Controller de Autenticação conseguir validar o usuário
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // NOVO MÉTODO: Define que usaremos BCrypt para não salvar senhas em texto puro
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> {
                    req.anyRequest().permitAll(); // LIBERA TUDO
                })
                // COMENTE a linha abaixo temporariamente para teste
                // .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
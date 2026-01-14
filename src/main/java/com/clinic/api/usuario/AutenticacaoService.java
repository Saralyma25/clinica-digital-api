package com.clinic.api.usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {

    private final UsuarioRepository repository;

    @Autowired
    public AutenticacaoService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca o usuário no banco pelo e-mail (que o Spring chama de username)
        UserDetails usuario = repository.findByEmail(username);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + username);
        }

        return usuario;
    }
}
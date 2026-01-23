package com.clinic.api.usuario;

import com.clinic.api.usuario.domain.UserRole;
import com.clinic.api.usuario.dto.UsuarioCadastroDto;
import jakarta.persistence.*;
import java.util.UUID;
import java.util.Objects;

@Table(name = "tb_usuario")
@Entity(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private Boolean ativo = true;

    // Construtor padrão (JPA obriga)
    public Usuario() {}

    // Construtor para criar a partir do DTO
    public Usuario(UsuarioCadastroDto dados) {
        this.email = dados.email();
        this.senha = dados.senha(); // OBS: Futuramente aplicaremos o Hash aqui
        this.role = dados.role();
        this.ativo = true;
    }

    // Getters e Setters Manuais (Sem Lombok)
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    // Método para Exclusão Lógica
    public void inativar() {
        this.ativo = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
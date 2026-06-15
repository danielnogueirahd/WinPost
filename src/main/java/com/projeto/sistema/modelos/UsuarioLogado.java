package com.projeto.sistema.modelos;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UsuarioLogado extends User {

    private static final long serialVersionUID = 1L;

    private Empresa empresa;
    private Long idUsuario;
    private String nome;
    private boolean superAdmin;

    public UsuarioLogado(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            Empresa empresa,
            Long idUsuario,
            String nome,
            boolean superAdmin) {

        super(username, password, true, true, true, true, authorities);

        this.empresa = empresa;
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.superAdmin = superAdmin;
    }

    // Construtor retrocompatível (sem parâmetro superAdmin explícito)
    public UsuarioLogado(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            Empresa empresa,
            Long idUsuario,
            String nome) {

        super(username, password, true, true, true, true, authorities);

        this.empresa = empresa;
        this.idUsuario = idUsuario;
        this.nome = nome;
        // Super Admin = usuário sem empresa vinculada OU com permissão CONFIGURACOES_SISTEMA
        this.superAdmin = (empresa == null) || authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("CONFIGURACOES_SISTEMA"));
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getNome() {
        return nome;
    }

    /**
     * Super Admin = perfil MASTER (sem empresa) OU com permissão CONFIGURACOES_SISTEMA.
     * Super Admins têm acesso irrestrito a todos os tenants.
     */
    public boolean isSuperAdmin() {
        return superAdmin;
    }

    /**
     * Retorna true se o usuário pertence a um tenant específico (empresa != null e não é superAdmin).
     * Tenant Admins só enxergam dados da sua própria empresa.
     */
    public boolean isTenantUser() {
        return !superAdmin && empresa != null;
    }
}
package com.projeto.sistema.modelos;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;


public class UsuarioLogado extends User {

    private static final long serialVersionUID = 1L;
    private Empresa empresa; 
    private Long idUsuario; 
    private String nome;
    
    public UsuarioLogado(String username, String password, Collection<? extends GrantedAuthority> authorities, Empresa empresa, Long idUsuario, String nome) { 
        super(username, password, true, true, true, true, authorities);
        this.empresa = empresa;
        this.idUsuario = idUsuario;
        this.nome = nome; 
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
}
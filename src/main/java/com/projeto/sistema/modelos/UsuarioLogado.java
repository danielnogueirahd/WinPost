package com.projeto.sistema.modelos;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

// 1. O "extends User" significa: "Esta classe tem tudo o que o utilizador 
// padrão do Spring tem, mas eu vou adicionar mais coisas".
public class UsuarioLogado extends User {

    private static final long serialVersionUID = 1L;
    
    // 2. AQUI ESTÁ O SEGREDO: Vamos guardar a empresa na memória!
    private Empresa empresa; 
    
    // (Opcional, mas muito útil) Guardar também o ID do usuário
    private Long idUsuario; 
    
    private String nome;

 // Atualizar o construtor para receber o nome:
    public UsuarioLogado(String username, String password, Collection<? extends GrantedAuthority> authorities, Empresa empresa, Long idUsuario) {
        super(username, password, true, true, true, true, authorities);
        this.empresa = empresa;
        this.idUsuario = idUsuario;
    }

    // 4. Getters para podermos pegar a empresa em qualquer lugar do sistema
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
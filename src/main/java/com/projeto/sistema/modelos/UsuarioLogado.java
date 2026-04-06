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

    // 3. O Construtor: É aqui que o crachá é fabricado na hora do login
    public UsuarioLogado(String username, String password, Collection<? extends GrantedAuthority> authorities, Empresa empresa, Long idUsuario) {
        
        // O 'super' entrega o nome, senha e permissões para o Spring tomar conta
        super(username, password, true, true, true, true, authorities);
        
        // E nós guardamos a empresa e o ID na nossa parte do crachá
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
}
package com.projeto.sistema.modelos;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "empresas")
public class Empresa implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String razaoSocial;
    
    @Column(unique = true)
    private String cnpj;

 // NOVO CAMPO: Controla a exclusão lógica (Soft Delete)
    @Column(nullable = false, columnDefinition = "BIT DEFAULT 1")
    private boolean ativo = true;

    // Getters e Setters originais...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    // NOVOS Getters e Setters para o campo 'ativo'
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
package com.projeto.sistema.modelos;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "perfis")
public class Perfil implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nome; // Ex: "ADMINISTRADOR", "ATENDENTE"

    /* * EXPLICAÇÃO DA MÁGICA ABAIXO:
     * Como uma pessoa pode ter VÁRIAS permissões, usamos uma Lista (List).
     * O @ElementCollection avisa o Hibernate: "Ei, crie uma tabelinha separada 
     * no banco de dados só para guardar as permissões desse perfil!"
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "perfil_permissoes", joinColumns = @JoinColumn(name = "perfil_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permissao")
    private List<Permissao> permissoes = new ArrayList<>();

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public List<Permissao> getPermissoes() { return permissoes; }
    public void setPermissoes(List<Permissao> permissoes) { this.permissoes = permissoes; }
}
package com.projeto.sistema.modelos;

import jakarta.persistence.*;

@Entity
public class TipoEvento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;      // Ex: "Academia"
    private String corHex;    // Ex: "#dc3545"
    private String icone;     // Ex: "fa-dumbbell"

    // --- CONSTRUTORES ---
    
    // Construtor vazio (Obrigatório para o JPA/Hibernate)
    public TipoEvento() {}

    // Construtor com dados (Facilita criar novos objetos)
    public TipoEvento(String nome, String corHex, String icone) {
        this.nome = nome;
        this.corHex = corHex;
        this.icone = icone;
    }

    // --- GETTERS E SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCorHex() {
        return corHex;
    }

    public void setCorHex(String corHex) {
        this.corHex = corHex;
    }

    public String getIcone() {
        return icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }
}
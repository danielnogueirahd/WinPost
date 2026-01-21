package com.projeto.sistema.modelos;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "lembretes")
public class Lembrete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    
    private String descricao;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    // Valores esperados: "TAREFA", "REUNIAO", "IMPORTANTE"
    private String tipo; 

    // Vínculo opcional com um contato existente (Chave Estrangeira)
    @ManyToOne
    @JoinColumn(name = "contato_id")
    private Contatos contato;

    // --- Construtores ---
    public Lembrete() {}

    // --- Getters e Setters ---
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    public LocalDateTime getDataHora() {
        return dataHora;
    }
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    public Contatos getContato() {
        return contato;
    }
    public void setContato(Contatos contato) {
        this.contato = contato;
    }
}
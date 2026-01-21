package com.projeto.sistema.modelos;

import java.time.LocalDate;

public class LembreteDTO {
    private String titulo;
    private String descricao;
    private String tipo; // "NIVER", "TAREFA", etc.
    private LocalDate dataEvento;
    private Long idReferencia; // ID do contato para criar link

    public LembreteDTO(String titulo, String descricao, String tipo, LocalDate dataEvento, Long idReferencia) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.tipo = tipo;
        this.dataEvento = dataEvento;
        this.idReferencia = idReferencia;
    }

    // Getters
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getTipo() { return tipo; }
    public LocalDate getDataEvento() { return dataEvento; }
    public Long getIdReferencia() { return idReferencia; }
}
package com.projeto.sistema.modelos;

import java.time.LocalDate;

public class EventoAgenda {

    private LocalDate data;
    private String tipo; // Vamos usar "NIVER" ou "ENVIO"
    private String titulo; // Ex: "Aniversário de João", "Promoção de Natal"
    private String cor; // "success" (verde) ou "primary" (azul)

    public EventoAgenda(LocalDate data, String tipo, String titulo, String cor) {
        this.data = data;
        this.tipo = tipo;
        this.titulo = titulo;
        this.cor = cor;
    }

    // --- Getters e Setters ---
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
}
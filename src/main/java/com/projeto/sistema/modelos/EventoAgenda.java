package com.projeto.sistema.modelos;

import java.time.LocalDateTime;

public class EventoAgenda {

    // AGORA USA LocalDateTime PARA GUARDAR A HORA!
    private LocalDateTime data; 
    private String tipo; 
    private String titulo; 
    private String cor; 
    private Empresa empresa; 

    public EventoAgenda(LocalDateTime data, String tipo, String titulo, String cor) {
        this.data = data;
        this.tipo = tipo;
        this.titulo = titulo;
        this.cor = cor;
    }

    // --- Getters e Setters ---
    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
   
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
}
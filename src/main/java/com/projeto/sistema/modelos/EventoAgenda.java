package com.projeto.sistema.modelos;

import java.time.LocalDateTime;

public class EventoAgenda {

    private Long id; // ID adicionado para o Thymeleaf e JS funcionarem!
    private LocalDateTime data; 
    private String tipo; 
    private String titulo; 
    private String classeCor; 
    private String cor; 
    private Empresa empresa; 

    // Construtor atualizado para casar perfeitamente com o AgendaControle
    public EventoAgenda(LocalDateTime data, String tipo, String titulo, String classeCor) {
        this.data = data;
        this.tipo = tipo;
        this.titulo = titulo;
        this.classeCor = classeCor;
    }

    // --- Getters e Setters ---

    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public LocalDateTime getData() { 
        return data; 
    }
    
    public void setData(LocalDateTime data) { 
        this.data = data; 
    }

    public String getTipo() { 
        return tipo; 
    }
    
    public void setTipo(String tipo) { 
        this.tipo = tipo; 
    }

    public String getTitulo() { 
        return titulo; 
    }
    
    public void setTitulo(String titulo) { 
        this.titulo = titulo; 
    }
    
    public String getClasseCor() { 
        return classeCor; 
    }
    
    public void setClasseCor(String classeCor) { 
        this.classeCor = classeCor; 
    }
    
    public String getCor() { 
        return cor; 
    }
    
    public void setCor(String cor) { 
        this.cor = cor; 
    }
   
    public Empresa getEmpresa() { 
        return empresa; 
    }
    
    public void setEmpresa(Empresa empresa) { 
        this.empresa = empresa; 
    }
}
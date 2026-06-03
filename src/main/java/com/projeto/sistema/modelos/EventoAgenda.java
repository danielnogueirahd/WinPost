package com.projeto.sistema.modelos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity // 1. Indica que esta classe é uma entidade do banco de dados
public class EventoAgenda {

    @Id // 2. Indica que este campo é a chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 3. Gera o ID automaticamente (auto-incremento)
    private Long id; 
    
    private LocalDateTime data; 
    private String tipo; 
    private String titulo; 
    private String classeCor; 
    private String cor; 
    
    @ManyToOne // 4. Você precisará mapear a relação com Empresa (ajuste conforme a regra do seu negócio)
    private Empresa empresa; 

    // Construtor vazio exigido pelo JPA/Hibernate
    public EventoAgenda() {
    }

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
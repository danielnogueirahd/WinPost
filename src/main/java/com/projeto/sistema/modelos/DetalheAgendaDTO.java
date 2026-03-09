package com.projeto.sistema.modelos;

public class DetalheAgendaDTO {
    private String tipo;      // Ex: "NIVER", "ENVIO", "FERIADO", "REUNIAO"
    private String titulo;    // Ex: "Reunião de Equipe"
    private String subtitulo; // Ex: "14:30 - Sala 1"
    private Long idRef;       // O ID da mensagem ou do evento (ESSENCIAL PARA O BOTÃO FUNCIONAR)

    // Construtor Completo
    public DetalheAgendaDTO(String tipo, String titulo, String subtitulo, Long idRef) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.idRef = idRef;
    }

    // Getters e Setters
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getSubtitulo() { return subtitulo; }
    public void setSubtitulo(String subtitulo) { this.subtitulo = subtitulo; }

    public Long getIdRef() { return idRef; }
    public void setIdRef(Long idRef) { this.idRef = idRef; }
}
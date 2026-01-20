package com.projeto.sistema.modelos;

public class DetalheAgendaDTO {
    private String tipo; // "NIVER", "ENVIO", "FERIADO"
    private String titulo; // Nome ou Assunto
    private String subtitulo; // Email ou Grupo
    private Long idRef; // ID do contato ou da mensagem (para links)

    public DetalheAgendaDTO(String tipo, String titulo, String subtitulo, Long idRef) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.idRef = idRef;
    }

    // Getters
    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getSubtitulo() { return subtitulo; }
    public Long getIdRef() { return idRef; }
}
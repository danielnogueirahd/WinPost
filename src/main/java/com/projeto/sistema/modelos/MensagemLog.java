package com.projeto.sistema.modelos;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "mensagens_log")
@EntityListeners(AuditingEntityListener.class)
public class MensagemLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assunto;

    // CORREÇÃO 1: "VARCHAR(MAX)" é o ideal para SQL Server (TEXT é antigo)
    @Column(columnDefinition = "VARCHAR(MAX)") 
    private String conteudo;

    private String nomeGrupoDestino;
    
    private Integer totalDestinatarios;

    // Salva os nomes dos arquivos separados por vírgula
    private String nomesAnexos; 

    private String status; // "SUCESSO", "ERRO"

    
    @Column(length = 50) 
    private String pasta = "ENVIADAS";
    // --------------------------------------------------------

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataEnvio;

    // CORREÇÃO 2: Campo organizado e com regra de não-nulo
    @Column(nullable = false)
    private boolean lida = false;
    
    @Column(columnDefinition = "BIT DEFAULT 0", nullable = false) 
    private boolean favorito = false;

    // --- Construtores ---
    public MensagemLog() {
        
    }

    // --- Getters e Setters ---
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAssunto() {
        return assunto;
    }
    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }
    public String getConteudo() {
        return conteudo;
    }
    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
    public String getNomeGrupoDestino() {
        return nomeGrupoDestino;
    }
    public void setNomeGrupoDestino(String nomeGrupoDestino) {
        this.nomeGrupoDestino = nomeGrupoDestino;
    }
    public Integer getTotalDestinatarios() {
        return totalDestinatarios;
    }
    public void setTotalDestinatarios(Integer totalDestinatarios) {
        this.totalDestinatarios = totalDestinatarios;
    }
    public String getNomesAnexos() {
        return nomesAnexos;
    }
    public void setNomesAnexos(String nomesAnexos) {
        this.nomesAnexos = nomesAnexos;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    
    // --- Getters e Setters da Pasta ---
    public String getPasta() {
        return pasta;
    }
    public void setPasta(String pasta) {
        this.pasta = pasta;
    }
    // ----------------------------------

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }
    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }
    
    // Método auxiliar para saber se tem anexo
    public boolean isTemAnexo() {
        return nomesAnexos != null && !nomesAnexos.isEmpty();
    }

    public boolean isLida() {
        return lida;
    }
    public void setLida(boolean lida) {
        this.lida = lida;
    }
    public boolean isFavorito() {
        return favorito;
    }

    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }
}
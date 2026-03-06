package com.projeto.sistema.modelos;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "contatos")
@EntityListeners(AuditingEntityListener.class)
public class Contatos implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- DADOS PESSOAIS ---

    @Column(length = 50)
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "O email é obrigatório")
    @Size(max = 255, message = "O email deve ter no máximo 255 caracteres")
    @Email(message = "Digite um email válido")
    private String email;

    @NotBlank(message = "O telefone é obrigatório")
    private String telefone;

    // --- ENDEREÇO ---

    @NotBlank(message = "O CEP é obrigatório")
    private String cep;

    @Column(length = 150)
    @NotBlank(message = "A rua é obrigatória")
    @Size(max = 150, message = "A rua deve ter no máximo 150 caracteres")
    private String rua;

    @NotNull(message = "O número é obrigatório")
    @Min(value = 1, message = "O número deve ser positivo")
    @Max(value = 999999, message = "O número deve ter no máximo 6 dígitos")
    private Integer numero;

    @Column(length = 100)
    @NotBlank(message = "O bairro é obrigatório")
    @Size(max = 100, message = "O bairro deve ter no máximo 100 caracteres")
    private String bairro;

    @Column(length = 100)
    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
    private String cidade;

    @NotBlank(message = "O estado é obrigatório")
    private String estado;

    private String complemento;

    // --- DATAS E CONFIGURAÇÕES ---

    @Column(name = "data_nascimento", length = 5)
    @Pattern(regexp = "^$|^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])$", message = "A data deve estar no formato DD/MM")
    private String dataNascimento;

    @CreatedDate
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDate dataCadastro;

    // MUDANÇA AQUI: Alterado de 'boolean' para 'Boolean' para aceitar NULL do banco
    @Column(name = "exibir_na_agenda")
    private Boolean exibirNaAgenda = false; 

    // --- RELACIONAMENTOS ---

    @ManyToMany
    @JoinTable(name = "contato_grupo", 
               joinColumns = @JoinColumn(name = "contato_id"), 
               inverseJoinColumns = @JoinColumn(name = "grupo_id"))
    private List<Grupo> grupos = new ArrayList<>();

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    // MUDANÇA AQUI: Getter seguro que transforma NULL em false
    public Boolean getExibirNaAgenda() {
        return exibirNaAgenda != null ? exibirNaAgenda : false;
    }

    public void setExibirNaAgenda(Boolean exibirNaAgenda) {
        this.exibirNaAgenda = exibirNaAgenda;
    }

    public List<Grupo> getGrupos() {
        return grupos;
    }

    public void setGrupos(List<Grupo> grupos) {
        this.grupos = grupos;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Contatos other = (Contatos) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
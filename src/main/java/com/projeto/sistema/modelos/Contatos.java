package com.projeto.sistema.modelos;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
// Imports de validação
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "contatos")
public class Contatos implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(length = 50) // Limita no Banco de Dados
	@Size(max = 50, message = "O nome deve ter no máximo 50 caracteres") // Validação do Spring

	@NotBlank(message = "O nome é obrigatório")
	private String nome;
	
	@Column(nullable = false, length = 255)
	@NotBlank(message = "O email é obrigatório")
	@Size(max = 255, message = "O email deve ter no máximo 255 caracteres")
	@Email(message = "Digite um email válido") // Valida se tem @ e formato correto
	private String email;
	
	@NotBlank(message = "O telefone é obrigatório")
	private String telefone;
	
	@NotBlank(message = "O CEP é obrigatório")
	private String cep;
	
	@Column(length = 150) // Banco de dados
    @NotBlank(message = "A rua é obrigatória")
    @Size(max = 150, message = "A rua deve ter no máximo 150 caracteres") // Java
    private String rua;
	
	@NotNull(message = "O número é obrigatório")
	private Integer numero; // <-- Mude de 'int' para 'Integer' para funcionar a validação de vazio
	
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
	
	// O complemento geralmente é opcional, mas se você quiser obrigar:
	// @NotBlank(message = "O complemento é obrigatório") 
	private String complemento;
	
	private LocalDate dataCadastro;

	
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
	public String getCep() {
		return cep;
	}
	public void setCep(String cep) {
		this.cep = cep;
	}
	public String getComplemento() {
		return complemento;
	}
	public void setComplemento(String complemento) {
		this.complemento = complemento;
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
	public String getTelefone() {
		return telefone;
	}
	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public LocalDate getDataCadastro() {
		return dataCadastro;
	}
	public void setDataCadastro(LocalDate dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	
	
	
	
	
	
}

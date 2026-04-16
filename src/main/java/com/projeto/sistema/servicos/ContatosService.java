package com.projeto.sistema.servicos;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.Empresa;
import com.projeto.sistema.modelos.UsuarioLogado;
import com.projeto.sistema.repositorios.ContatosRepositorio;

@Service
@Transactional // Garante que se houver erro no meio do processo, ele desfaz a operação no banco
public class ContatosService {

	@Autowired
	private ContatosRepositorio contatosRepositorio;

	/**
	 * TAREFA 1: A REGRA DE OURO (Salvar)
	 * Antes de salvar, descobre quem está logado e injeta a empresa no contato.
	 */
	public Contatos salvar(Contatos contato) {
		// 1. Pega o usuário que está logado neste exato momento
		UsuarioLogado usuarioLogado = (UsuarioLogado) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		// 2. Pega a empresa deste usuário
		Empresa empresaDoUsuario = usuarioLogado.getEmpresa();

		// 3. Regra Multi-Empresas: Todo contato PRECISA de uma empresa
		if (empresaDoUsuario != null) {
			contato.setEmpresa(empresaDoUsuario);
		} else {
			// Lembra do Paradoxo do Admin Master? Se ele não tem empresa, não pode salvar contatos soltos.
			throw new RuntimeException("Operação negada: Apenas utilizadores vinculados a uma empresa podem registar contactos.");
		}

		// 4. Agora sim, salva no banco com segurança!
		return contatosRepositorio.save(contato);
	}

	/**
	 * TAREFA 2: Buscar apenas os contatos da Empresa Logada
	 */
	public List<Contatos> listarTodosDaEmpresaLogada() {
		UsuarioLogado usuarioLogado = (UsuarioLogado) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Empresa empresaDoUsuario = usuarioLogado.getEmpresa();

		if (empresaDoUsuario != null) {
			// O Repositório precisa ter este método (já falamos dele!)
			return contatosRepositorio.findByEmpresa(empresaDoUsuario); 
		} else {
			// Se for o Admin Master (sem empresa), retorna todos os contatos do sistema (ou você pode bloquear)
			return contatosRepositorio.findAll();
		}
	}

	/**
	 * Buscar um contato específico para Editar
	 */
	public Contatos buscarPorId(Long id) {
		Optional<Contatos> contato = contatosRepositorio.findById(id);
		
		// Opcional: Aqui seria o lugar perfeito para verificar se o contato encontrado 
		// realmente pertence à empresa do usuário logado, evitando que alguém tente editar 
		// um contato mudando o ID na URL!
		
		return contato.orElse(null);
	}

	/**
	 * Excluir Contato
	 */
	public void excluir(Long id) {
		contatosRepositorio.deleteById(id);
	}
}
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
@Transactional // Garante que se houver erro no meio do processo, ele desfaz a operação no
				// banco
public class ContatosService {

	@Autowired
	private ContatosRepositorio contatosRepositorio;

	/**
	 * TAREFA 1: A REGRA DE OURO (Salvar) Antes de salvar, descobre quem está logado
	 * e injeta a empresa no contato.
	 */
	public Contatos salvar(Contatos contato) {
		// 1. Pega o usuário que está logado neste exato momento
		UsuarioLogado usuarioLogado = (UsuarioLogado) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		// 2. Pega a empresa deste usuário
		Empresa empresaDoUsuario = usuarioLogado.getEmpresa();

		// 3. Regra Multi-Empresas: Todo contato PRECISA de uma empresa
		if (empresaDoUsuario != null) {
			contato.setEmpresa(empresaDoUsuario);
		} else {
			// Lembra do Paradoxo do Admin Master? Se ele não tem empresa, não pode salvar
			// contatos soltos.
			throw new RuntimeException(
					"Operação negada: Apenas utilizadores vinculados a uma empresa podem registar contactos.");
		}

		// 4. Agora sim, salva no banco com segurança!
		return contatosRepositorio.save(contato);
	}

	/**
	 * TAREFA 2: Buscar apenas os contatos da Empresa Logada (Listagem Simples)
	 */
	public List<Contatos> listarTodosDaEmpresaLogada() {
		UsuarioLogado usuarioLogado = (UsuarioLogado) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Empresa empresaDoUsuario = usuarioLogado.getEmpresa();

		if (empresaDoUsuario != null) {
			return contatosRepositorio.findByEmpresa(empresaDoUsuario);
		} else {
			return contatosRepositorio.findAll();
		}
	}

	/**
	 * TAREFA 2.1: Busca Dinâmica (Trata os filtros da tela de Listar Contatos) Este
	 * é o método que o seu ContatosControle vai chamar ao carregar a página e ao
	 * pesquisar.
	 */
	public List<Contatos> buscar(String nome, String cidade, Long grupoId, Empresa empresa) {

		// 1. Limpar os valores que vêm sujos do HTML
		if (nome != null && nome.trim().isEmpty()) {
			nome = null;
		}
		if (cidade != null && cidade.trim().isEmpty()) {
			cidade = null;
		}
		// Se o grupoId vier como 0 (Todos os Grupos), transformamos em null para o
		// Hibernate ignorar
		if (grupoId != null && grupoId == 0L) {
			grupoId = null;
		}

		// 2. Se não há nenhum filtro preenchido (ex: o utilizador apenas abriu a tela)
		if (nome == null && cidade == null && grupoId == null) {
			// Traz todos os contatos da empresa (isto garante que o novo contato aparece
			// logo!)
			return contatosRepositorio.findByEmpresa(empresa);
		}

		// 3. Se o utilizador digitou algo ou escolheu um grupo, fazemos a busca
		// avançada
		return contatosRepositorio.filtrarBusca(nome, cidade, grupoId, empresa);
	}

	/**
	 * Buscar um contato específico para Editar
	 */
	public Contatos buscarPorId(Long id) {
		Optional<Contatos> contato = contatosRepositorio.findById(id);
		return contato.orElse(null);
	}

	/**
	 * Excluir Contato
	 */
	public void excluir(Long id) {
		contatosRepositorio.deleteById(id);
	}

	/**
	 * Excluir Contato com Validação de Empresa
	 */
	public void excluirComSeguranca(Long id, Empresa empresaLogada) {
		Optional<Contatos> contato = contatosRepositorio.findById(id);

		if (contato.isPresent()) {
			// Regra de Ouro: Só apaga se for da mesma empresa
			if (contato.get().getEmpresa().getId().equals(empresaLogada.getId())) {
				contatosRepositorio.delete(contato.get());
			} else {
				throw new RuntimeException("Acesso negado: Você não tem permissão para excluir este contato.");
			}
		}
	}
}
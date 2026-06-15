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
@Transactional
public class ContatosService {

    @Autowired
    private ContatosRepositorio contatosRepositorio;

    /**
     * Salva contato garantindo que pertence à empresa do usuário logado.
     * Super Admin não pode salvar contatos sem empresa definida.
     */
    public Contatos salvar(Contatos contato) {
        UsuarioLogado usuarioLogado = getUsuarioLogado();
        Empresa empresaDoUsuario = usuarioLogado.getEmpresa();

        if (!usuarioLogado.isSuperAdmin()) {
            // Tenant Admin: força empresa própria, ignora qualquer empresa que venha do form
            if (empresaDoUsuario == null) {
                throw new RuntimeException(
                        "Operação negada: Usuário sem empresa vinculada não pode registrar contatos.");
            }
            contato.setEmpresa(empresaDoUsuario);
        } else {
            // Super Admin: deve ter empresa definida no objeto (selecionada no form ou herdada)
            if (contato.getEmpresa() == null || contato.getEmpresa().getId() == null) {
                throw new RuntimeException(
                        "Operação negada: Selecione a empresa para vincular o contato.");
            }
        }

        return contatosRepositorio.save(contato);
    }

    /**
     * Busca dinâmica filtrada pela empresa do usuário logado.
     * Super Admin pode filtrar por qualquer empresa ou ver todos.
     */
    public List<Contatos> buscar(String nome, String cidade, Long grupoId, Empresa empresa) {
        UsuarioLogado usuarioLogado = getUsuarioLogado();

        if (nome != null && nome.trim().isEmpty()) nome = null;
        if (cidade != null && cidade.trim().isEmpty()) cidade = null;
        if (grupoId != null && grupoId == 0L) grupoId = null;

        if (usuarioLogado.isSuperAdmin()) {
            // Super Admin: usa query irrestrita com filtro de empresa opcional
            Long empresaId = (empresa != null) ? empresa.getId() : null;
            if (nome == null && cidade == null && grupoId == null && empresaId == null) {
                return contatosRepositorio.findAll();
            }
            return contatosRepositorio.filtrarBuscaSuperAdmin(nome, cidade, grupoId, empresaId);
        }

        // Tenant: sempre filtra pela empresa
        if (nome == null && cidade == null && grupoId == null) {
            return contatosRepositorio.findByEmpresa(empresa);
        }
        return contatosRepositorio.filtrarBusca(nome, cidade, grupoId, empresa);
    }

    /**
     * Lista todos os contatos da empresa logada.
     */
    public List<Contatos> listarTodosDaEmpresaLogada() {
        UsuarioLogado usuarioLogado = getUsuarioLogado();

        if (usuarioLogado.isSuperAdmin()) {
            return contatosRepositorio.findAll();
        }
        return contatosRepositorio.findByEmpresa(usuarioLogado.getEmpresa());
    }

    /**
     * Busca contato por ID verificando que pertence à empresa do usuário logado.
     */
    public Contatos buscarPorId(Long id) {
        Optional<Contatos> contato = contatosRepositorio.findById(id);
        if (contato.isEmpty()) return null;

        UsuarioLogado usuarioLogado = getUsuarioLogado();
        if (!usuarioLogado.isSuperAdmin()) {
            Empresa empresa = usuarioLogado.getEmpresa();
            if (contato.get().getEmpresa() == null ||
                    !contato.get().getEmpresa().getId().equals(empresa.getId())) {
                throw new RuntimeException("Acesso negado: Contato não pertence à sua empresa.");
            }
        }

        return contato.get();
    }

    /**
     * Exclui contato com verificação de propriedade da empresa.
     */
    public void excluir(Long id) {
        excluirComSeguranca(id, getUsuarioLogado().getEmpresa());
    }

    /**
     * Exclui contato verificando que pertence à empresa informada.
     * Super Admin pode excluir de qualquer empresa.
     */
    public void excluirComSeguranca(Long id, Empresa empresaLogada) {
        Optional<Contatos> contato = contatosRepositorio.findById(id);

        if (contato.isEmpty()) return;

        UsuarioLogado usuarioLogado = getUsuarioLogado();
        if (!usuarioLogado.isSuperAdmin()) {
            if (contato.get().getEmpresa() == null ||
                    !contato.get().getEmpresa().getId().equals(empresaLogada.getId())) {
                throw new RuntimeException("Acesso negado: Você não tem permissão para excluir este contato.");
            }
        }

        contatosRepositorio.delete(contato.get());
    }

    private UsuarioLogado getUsuarioLogado() {
        return (UsuarioLogado) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
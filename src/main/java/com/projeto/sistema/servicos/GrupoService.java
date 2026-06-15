package com.projeto.sistema.servicos;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.Empresa;
import com.projeto.sistema.modelos.Grupo;
import com.projeto.sistema.modelos.UsuarioLogado;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.GrupoRepositorio;

@Service
@Transactional
public class GrupoService {

    @Autowired
    private GrupoRepositorio grupoRepositorio;

    @Autowired
    private ContatosRepositorio contatosRepositorio;

    public void salvarGrupoComContatos(Grupo grupo, List<Long> idsContatos) {
        if (grupo.getAtivo() == null) grupo.setAtivo(true);
        if (grupo.getIsMaster() == null) grupo.setIsMaster(false);

        // Garante que o grupo fica vinculado à empresa do usuário logado se não for SA
        UsuarioLogado usuarioLogado = getUsuarioLogado();
        if (!usuarioLogado.isSuperAdmin()) {
            grupo.setEmpresa(usuarioLogado.getEmpresa());
        }

        Grupo grupoSalvo = grupoRepositorio.save(grupo);

        if (idsContatos != null && !idsContatos.isEmpty()) {
            List<Contatos> contatosSelecionados = contatosRepositorio.findAllById(idsContatos);

            for (Contatos contato : contatosSelecionados) {
                // Segurança: apenas contatos da mesma empresa podem entrar no grupo
                if (pertenceMesmaEmpresa(contato.getEmpresa(), grupoSalvo.getEmpresa())) {
                    if (!contato.getGrupos().contains(grupoSalvo)) {
                        contato.getGrupos().add(grupoSalvo);
                        contatosRepositorio.save(contato);
                    }
                }
            }
        }
    }

    public void excluirGrupo(Long id) {
        Optional<Grupo> grupoOpt = grupoRepositorio.findById(id);
        if (grupoOpt.isEmpty()) return;

        Grupo grupo = grupoOpt.get();

        // Verifica posse do grupo antes de excluir
        UsuarioLogado usuarioLogado = getUsuarioLogado();
        if (!usuarioLogado.isSuperAdmin() &&
                !pertenceMesmaEmpresa(grupo.getEmpresa(), usuarioLogado.getEmpresa())) {
            throw new RuntimeException("Acesso negado: Grupo não pertence à sua empresa.");
        }

        List<Contatos> contatosDoGrupo = grupo.getContatos();
        if (contatosDoGrupo != null) {
            for (Contatos contato : contatosDoGrupo) {
                contato.getGrupos().remove(grupo);
                contatosRepositorio.save(contato);
            }
        }

        grupoRepositorio.delete(grupo);
    }

    private boolean pertenceMesmaEmpresa(Empresa e1, Empresa e2) {
        if (e1 == null || e2 == null) return false;
        return e1.getId().equals(e2.getId());
    }

    private UsuarioLogado getUsuarioLogado() {
        return (UsuarioLogado) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
package com.projeto.sistema.servicos;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.Grupo;
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
        
        // TRAVAS DE SEGURANÇA: Preenchendo campos obrigatórios do banco
        if (grupo.getAtivo() == null) {
            grupo.setAtivo(true);
        }
        
        // Nova trava para o is_master
        if (grupo.getIsMaster() == null) {
            grupo.setIsMaster(false);
        }

        // 1. Salva o grupo
        Grupo grupoSalvo = grupoRepositorio.save(grupo);

        // 2. Faz o vínculo dos contatos
        if (idsContatos != null && !idsContatos.isEmpty()) {
            List<Contatos> contatosSelecionados = contatosRepositorio.findAllById(idsContatos);
            
            for (Contatos contato : contatosSelecionados) {
                if (!contato.getGrupos().contains(grupoSalvo)) {
                    contato.getGrupos().add(grupoSalvo);
                    contatosRepositorio.save(contato);
                }
            }
        }
    }

    public void excluirGrupo(Long id) {
        Optional<Grupo> grupoOpt = grupoRepositorio.findById(id);
        
        if (grupoOpt.isPresent()) {
            Grupo grupo = grupoOpt.get();
            
            List<Contatos> contatosDoGrupo = grupo.getContatos();
            if (contatosDoGrupo != null) {
                for (Contatos contato : contatosDoGrupo) {
                    contato.getGrupos().remove(grupo);
                    contatosRepositorio.save(contato);
                }
            }
            
            grupoRepositorio.delete(grupo);
        }
    }
}
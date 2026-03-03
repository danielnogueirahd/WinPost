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
public class GrupoService {

    @Autowired
    private GrupoRepositorio grupoRepositorio;

    @Autowired
    private ContatosRepositorio contatosRepositorio;

    // MÉTODO NOVO E CORRIGIDO
    @Transactional
    public void salvarGrupoComContatos(Grupo grupo, List<Long> idsContatos) {
        // 1. Salva o grupo primeiro para gerar o ID
        Grupo grupoSalvo = grupoRepositorio.save(grupo);

        // 2. Se houver contatos selecionados, faz o vínculo
        if (idsContatos != null && !idsContatos.isEmpty()) {
            List<Contatos> contatosSelecionados = contatosRepositorio.findAllById(idsContatos);
            
            for (Contatos contato : contatosSelecionados) {
                // Adiciona este grupo na lista de grupos do contato
                // (O Java precisa que a gente adicione nos DOIS lados se for bidirecional, 
                // mas salvar o 'Lado Proprietário' da relação é o mais importante)
                if (!contato.getGrupos().contains(grupoSalvo)) {
                    contato.getGrupos().add(grupoSalvo);
                    contatosRepositorio.save(contato); // Salva o contato atualizado
                }
            }
        }
    }

    @Transactional
    public void excluirGrupo(Long idGrupo) {
        Optional<Grupo> grupoOpt = grupoRepositorio.findById(idGrupo);
        
        if (grupoOpt.isPresent()) {
            Grupo grupo = grupoOpt.get();
            // Remove as referências antes de excluir
            for (Contatos contato : grupo.getContatos()) {
                contato.getGrupos().remove(grupo);
                contatosRepositorio.save(contato);
            }
            grupoRepositorio.delete(grupo);
        }
    }
}
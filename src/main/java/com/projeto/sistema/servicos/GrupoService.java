package com.projeto.sistema.servicos;

import java.util.ArrayList;
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

    @Transactional
    public Grupo criarGrupoComContatos(String nomeGrupo, List<Long> idsContatos) {
        Grupo novoGrupo = new Grupo();
        novoGrupo.setNome(nomeGrupo);
        novoGrupo = grupoRepositorio.save(novoGrupo);

        if (idsContatos != null && !idsContatos.isEmpty()) {
            List<Contatos> contatosSelecionados = contatosRepositorio.findAllById(idsContatos);
            for (Contatos contato : contatosSelecionados) {
                if (contato.getGrupos() == null) {
                    contato.setGrupos(new ArrayList<>());
                }
                contato.getGrupos().add(novoGrupo);
                contatosRepositorio.save(contato);
            }
        }
        return novoGrupo;
    }

    // NOVO MÉTODO: Excluir Grupo com segurança
    @Transactional
    public void excluirGrupo(Long idGrupo) {
        Optional<Grupo> grupoOpt = grupoRepositorio.findById(idGrupo);
        
        if (grupoOpt.isPresent()) {
            Grupo grupo = grupoOpt.get();
            
            // 1. Remove o grupo da lista de grupos de cada contato (Desvinculação)
            // Isso apaga o registro na tabela de junção (contato_grupo)
            for (Contatos contato : grupo.getContatos()) {
                contato.getGrupos().remove(grupo);
                contatosRepositorio.save(contato);
            }
            
            // 2. Agora pode excluir o grupo sem erro de FK
            grupoRepositorio.delete(grupo);
        }
    }
}
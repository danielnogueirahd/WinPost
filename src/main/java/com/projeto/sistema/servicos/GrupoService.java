package com.projeto.sistema.servicos;

import java.util.ArrayList;
import java.util.List;

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

    @Transactional // Garante que, se der erro no meio, ele cancela tudo (Rollback)
    public Grupo criarGrupoComContatos(String nomeGrupo, List<Long> idsContatos) {
        
        // 1. Criar e Salvar o novo Grupo
        Grupo novoGrupo = new Grupo();
        novoGrupo.setNome(nomeGrupo);
        // novoGrupo.setDescricao("Criado via filtro rápido"); // Opcional
        novoGrupo = grupoRepositorio.save(novoGrupo);

        // 2. Buscar os contatos pelo ID
        List<Contatos> contatosSelecionados = contatosRepositorio.findAllById(idsContatos);

        // 3. Associar o grupo a cada contato
        for (Contatos contato : contatosSelecionados) {
            // Verifica se a lista está nula para evitar erro
            if (contato.getGrupos() == null) {
                contato.setGrupos(new ArrayList<>());
            }
            
            contato.getGrupos().add(novoGrupo);
            contatosRepositorio.save(contato); // Atualiza a relação no banco
        }

        return novoGrupo;
    }
}
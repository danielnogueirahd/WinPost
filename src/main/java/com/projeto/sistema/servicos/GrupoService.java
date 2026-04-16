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

    // Precisamos do repositório de contatos para fazer os vínculos!
    @Autowired
    private ContatosRepositorio contatosRepositorio;

    /**
     * Método que salva o grupo e já vincula os contatos escolhidos na tela.
     */
    public void salvarGrupoComContatos(Grupo grupo, List<Long> idsContatos) {
        // 1. Salva o grupo primeiro para garantir que ele tem um ID (no banco)
        Grupo grupoSalvo = grupoRepositorio.save(grupo);

        // 2. Se o usuário selecionou algum contato no cadastro, fazemos o vínculo
        if (idsContatos != null && !idsContatos.isEmpty()) {
            List<Contatos> contatosSelecionados = contatosRepositorio.findAllById(idsContatos);
            
            for (Contatos contato : contatosSelecionados) {
                // Adiciona o grupo salvo à lista de grupos do contato
                if (!contato.getGrupos().contains(grupoSalvo)) {
                    contato.getGrupos().add(grupoSalvo);
                    contatosRepositorio.save(contato);
                }
            }
        }
    }

    /**
     * Método que exclui o grupo com segurança, desvinculando os contatos antes.
     */
    public void excluirGrupo(Long id) {
        Optional<Grupo> grupoOpt = grupoRepositorio.findById(id);
        
        if (grupoOpt.isPresent()) {
            Grupo grupo = grupoOpt.get();
            
            // Passo de Segurança: Desvincular o grupo de todos os contatos 
            // que pertencem a ele antes de apagar, para evitar erro de SQL (Foreign Key)
            List<Contatos> contatosDoGrupo = grupo.getContatos();
            if (contatosDoGrupo != null) {
                for (Contatos contato : contatosDoGrupo) {
                    contato.getGrupos().remove(grupo);
                    contatosRepositorio.save(contato);
                }
            }
            
            // Após soltar as amarras, podemos excluir o grupo tranquilamente!
            grupoRepositorio.delete(grupo);
        }
    }
    
    // (Se você tiver outros métodos aqui como listarGruposPermitidos(), pode mantê-los!)
}
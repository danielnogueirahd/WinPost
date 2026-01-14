package com.projeto.sistema.controle;

import java.util.List;
import java.util.Optional; // Import necessário

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Contatos; // Import necessário
import com.projeto.sistema.modelos.Grupo;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.GrupoRepositorio;
import com.projeto.sistema.servicos.GrupoService;

@Controller
@RequestMapping("/grupos")
public class GrupoControle {

    @Autowired
    private GrupoService grupoService;
  
    @Autowired
    private ContatosRepositorio contatosRepositorio;
    
    @Autowired
    private GrupoRepositorio grupoRepositorio;

    // 1. TELA DE CADASTRO (Mantida com ajustes de navegação)
    @GetMapping("/cadastro")
    public ModelAndView cadastrar(Grupo grupo,
                                  @RequestParam(value = "mesAniversario", required = false) Integer mesAniversario) {
        
        ModelAndView mv = new ModelAndView("grupos/cadastro");
        mv.addObject("grupo", grupo);
        mv.addObject("paginaAtiva", "cadastroGrupo"); // Marcador para a Sidebar
        
        if (mesAniversario != null) {
            mv.addObject("listaContatos", contatosRepositorio.findByMesAniversario(mesAniversario));
            mv.addObject("mesSelecionado", mesAniversario);
            mv.addObject("filtrado", true); 
        } else {
            mv.addObject("listaContatos", contatosRepositorio.findAll());
        }
        
        // OBS: Removi "listaGruposExistentes" daqui, pois agora ela vive na tela "gerenciar"
        
        return mv;
    }

    // 2. SALVAR NOVO GRUPO (Atualizado redirecionamento)
    @PostMapping("/salvar")
    public ModelAndView salvar(Grupo grupo, 
                               @RequestParam(value = "idsContatos", required = false) List<Long> idsContatos,
                               RedirectAttributes attributes) {
        try {
            if (idsContatos != null && !idsContatos.isEmpty()) {
                grupoService.criarGrupoComContatos(grupo.getNome(), idsContatos);
            } else {
                grupoRepositorio.save(grupo);
            }
            attributes.addFlashAttribute("mensagemSucesso", "Grupo '" + grupo.getNome() + "' salvo com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("mensagemErro", "Erro ao salvar grupo.");
        }
        // ALTERADO: Agora redireciona para a lista de gerenciamento
        return new ModelAndView("redirect:/grupos/gerenciar");
    }

    // 3. TELA DE GERENCIAR (NOVO - Lista todos os grupos)
    @GetMapping("/gerenciar")
    public ModelAndView gerenciar() {
        ModelAndView mv = new ModelAndView("grupos/gerenciar");
        mv.addObject("listaGrupos", grupoRepositorio.findAll());
        mv.addObject("paginaAtiva", "gerenciarGrupo"); // Marcador para a Sidebar
        return mv;
    }

    // 4. TELA DE EDIÇÃO (NOVO - Abre um grupo específico)
    @GetMapping("/editar/{id}")
    public ModelAndView editar(@PathVariable("id") Long id) {
        Optional<Grupo> grupoOpt = grupoRepositorio.findById(id);
        
        if (grupoOpt.isPresent()) {
            ModelAndView mv = new ModelAndView("grupos/editar");
            mv.addObject("grupo", grupoOpt.get());
            // Carrega todos os contatos para o dropdown de "Adicionar Novos Membros"
            mv.addObject("todosContatos", contatosRepositorio.findAll());
            mv.addObject("paginaAtiva", "gerenciarGrupo");
            return mv;
        }
        return new ModelAndView("redirect:/grupos/gerenciar");
    }

    // 5. ATUALIZAR GRUPO (NOVO - Salva edições e adiciona membros)
    @PostMapping("/atualizar")
    public ModelAndView atualizar(Grupo grupo, 
                                  @RequestParam(value = "novosMembros", required = false) List<Long> novosMembros,
                                  RedirectAttributes attributes) {
        try {
            // Salva nome/descrição
            Grupo grupoSalvo = grupoRepositorio.save(grupo);
            
            // Lógica para adicionar novos membros selecionados na tela de edição
            if(novosMembros != null && !novosMembros.isEmpty()) {
                List<Contatos> contatosAdd = contatosRepositorio.findAllById(novosMembros);
                for(Contatos c : contatosAdd) {
                    // Só adiciona se ainda não estiver no grupo
                    if(!c.getGrupos().contains(grupoSalvo)) {
                         c.getGrupos().add(grupoSalvo);
                         contatosRepositorio.save(c);
                    }
                }
            }
            attributes.addFlashAttribute("mensagemSucesso", "Grupo atualizado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("mensagemErro", "Erro ao atualizar grupo.");
        }
        return new ModelAndView("redirect:/grupos/editar/" + grupo.getId());
    }

    // 6. REMOVER MEMBRO (NOVO - Remove uma pessoa específica do grupo)
    @GetMapping("/removerMembro/{grupoId}/{contatoId}")
    public ModelAndView removerMembro(@PathVariable("grupoId") Long grupoId, 
                                      @PathVariable("contatoId") Long contatoId,
                                      RedirectAttributes attributes) {
        try {
            Optional<Contatos> contatoOpt = contatosRepositorio.findById(contatoId);
            Optional<Grupo> grupoOpt = grupoRepositorio.findById(grupoId);

            if(contatoOpt.isPresent() && grupoOpt.isPresent()) {
                Contatos contato = contatoOpt.get();
                Grupo grupo = grupoOpt.get();
                
                // Remove a relação
                contato.getGrupos().remove(grupo);
                contatosRepositorio.save(contato);
                
                attributes.addFlashAttribute("mensagemSucesso", "Membro removido do grupo.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("mensagemErro", "Erro ao remover membro.");
        }
        // Volta para a mesma tela de edição
        return new ModelAndView("redirect:/grupos/editar/" + grupoId);
    }

    // 7. EXCLUIR GRUPO (Atualizado redirecionamento)
    @GetMapping("/excluir/{id}")
    public ModelAndView excluir(@PathVariable("id") Long id, RedirectAttributes attributes) {
        try {
            grupoService.excluirGrupo(id);
            attributes.addFlashAttribute("mensagemSucesso", "Grupo excluído com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("mensagemErro", "Erro ao excluir grupo.");
        }
        // ALTERADO: Agora redireciona para a lista de gerenciamento
        return new ModelAndView("redirect:/grupos/gerenciar");
    }
}
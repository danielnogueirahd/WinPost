package com.projeto.sistema.controle;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/cadastro")
    public ModelAndView cadastrar(Grupo grupo) {
        ModelAndView mv = new ModelAndView("grupos/cadastro"); // Note que deve ser o caminho correto do seu HTML
        mv.addObject("grupo", grupo);
        mv.addObject("listaContatos", contatosRepositorio.findAll());
        
        // NOVO: Envia a lista de grupos já criados para exibir na tabela de exclusão
        mv.addObject("listaGruposExistentes", grupoRepositorio.findAll());
        
        return mv;
    }

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
        }
        return new ModelAndView("redirect:/grupos/cadastro");
    }

    // NOVO: Rota de Exclusão
    @GetMapping("/excluir/{id}")
    public ModelAndView excluir(@PathVariable("id") Long id, RedirectAttributes attributes) {
        try {
            grupoService.excluirGrupo(id);
            attributes.addFlashAttribute("mensagemSucesso", "Grupo excluído com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("mensagemErro", "Erro ao excluir grupo.");
        }
        return new ModelAndView("redirect:/grupos/cadastro");
    }
}
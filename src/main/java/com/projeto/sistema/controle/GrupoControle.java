package com.projeto.sistema.controle;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.Grupo;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.GrupoRepositorio;
import com.projeto.sistema.servicos.EmailService; // Importação do serviço de e-mail
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

    @Autowired
    private EmailService emailService; // Serviço injetado corretamente
    
    @GetMapping("/cadastro")
    public ModelAndView cadastrar(Grupo grupo,
                                  @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                                  @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        ModelAndView mv = new ModelAndView("grupos/cadastro");
        mv.addObject("grupo", grupo);
        mv.addObject("paginaAtiva", "cadastroGrupo"); 
        
        // Mantém as datas no formulário para o usuário ver o que filtrou
        mv.addObject("dataInicio", dataInicio);
        mv.addObject("dataFim", dataFim);

        if (dataInicio != null && dataFim != null) {
            // Usa o novo método de busca por período
            mv.addObject("listaContatos", contatosRepositorio.findByAniversarioNoPeriodo(dataInicio, dataFim));
            mv.addObject("filtrado", true); 
        } else {
            // Se não filtrar, traz todos (ou poderia trazer ninguém para a tela ficar limpa)
            mv.addObject("listaContatos", contatosRepositorio.findAll());
        }
        
        return mv;
    }

    // 2. SALVAR NOVO GRUPO
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
        return new ModelAndView("redirect:/grupos/gerenciar");
    }

    // 3. TELA DE GERENCIAR
    @GetMapping("/gerenciar")
    public ModelAndView gerenciar() {
        ModelAndView mv = new ModelAndView("grupos/gerenciar");
        mv.addObject("listaGrupos", grupoRepositorio.findAll());
        mv.addObject("paginaAtiva", "gerenciarGrupo");
        return mv;
    }

    // 4. TELA DE EDIÇÃO
    @GetMapping("/editar/{id}")
    public ModelAndView editar(@PathVariable("id") Long id) {
        Optional<Grupo> grupoOpt = grupoRepositorio.findById(id);
        
        if (grupoOpt.isPresent()) {
            ModelAndView mv = new ModelAndView("grupos/editar");
            mv.addObject("grupo", grupoOpt.get());
            mv.addObject("todosContatos", contatosRepositorio.findAll());
            mv.addObject("paginaAtiva", "gerenciarGrupo");
            return mv;
        }
        return new ModelAndView("redirect:/grupos/gerenciar");
    }

    // 5. ATUALIZAR GRUPO
    @PostMapping("/atualizar")
    public ModelAndView atualizar(Grupo grupo, 
                                  @RequestParam(value = "novosMembros", required = false) List<Long> novosMembros,
                                  RedirectAttributes attributes) {
        try {
            Grupo grupoSalvo = grupoRepositorio.save(grupo);
            
            if(novosMembros != null && !novosMembros.isEmpty()) {
                List<Contatos> contatosAdd = contatosRepositorio.findAllById(novosMembros);
                for(Contatos c : contatosAdd) {
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

    // 6. REMOVER MEMBRO
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
                
                contato.getGrupos().remove(grupo);
                contatosRepositorio.save(contato);
                
                attributes.addFlashAttribute("mensagemSucesso", "Membro removido do grupo.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("mensagemErro", "Erro ao remover membro.");
        }
        return new ModelAndView("redirect:/grupos/editar/" + grupoId);
    }

    // 7. EXCLUIR GRUPO
    @GetMapping("/excluir/{id}")
    public ModelAndView excluir(@PathVariable("id") Long id, RedirectAttributes attributes) {
        try {
            grupoService.excluirGrupo(id);
            attributes.addFlashAttribute("mensagemSucesso", "Grupo excluído com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("mensagemErro", "Erro ao excluir grupo.");
        }
        return new ModelAndView("redirect:/grupos/gerenciar");
    }
   
    // 8. DISPARAR AÇÃO (E-MAIL EM MASSA)
    @PostMapping("/disparar")
    public ModelAndView dispararAcao(@RequestParam("grupoId") Long grupoId,
                                     @RequestParam("assunto") String assunto,
                                     @RequestParam("mensagem") String mensagem,
                                     @RequestParam(value = "anexos", required = false) MultipartFile[] anexos, // Recebe arquivos
                                     RedirectAttributes attributes) {
        try {
            Optional<Grupo> grupoOpt = grupoRepositorio.findById(grupoId);
            
            if (grupoOpt.isPresent()) {
                Grupo grupo = grupoOpt.get();
                if (grupo.getContatos().isEmpty()) {
                    attributes.addFlashAttribute("mensagemErro", "O grupo está vazio.");
                } else {
                    // Chama o novo método que envia e loga
                    emailService.enviarDisparo(grupo, assunto, mensagem, anexos);
                    
                    attributes.addFlashAttribute("mensagemSucesso", "Envio iniciado em segundo plano para " + grupo.getNome());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("mensagemErro", "Erro ao processar envio.");
        }
        return new ModelAndView("redirect:/grupos/gerenciar");
    }
}
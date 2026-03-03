package com.projeto.sistema.controle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult; // Importante para o @Valid funcionar
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
import com.projeto.sistema.servicos.EmailService;
import com.projeto.sistema.servicos.GrupoService;

import jakarta.validation.Valid;

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
    private EmailService emailService;
    
    // 1. TELA DE CADASTRO
    @GetMapping("/cadastro")
    public ModelAndView cadastrar(Grupo grupo,
                                  @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                                  @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        ModelAndView mv = new ModelAndView("grupos/cadastro");
        mv.addObject("grupo", grupo);
        mv.addObject("paginaAtiva", "cadastroGrupo"); 
        
        mv.addObject("dataInicio", dataInicio);
        mv.addObject("dataFim", dataFim);

        if (dataInicio != null && dataFim != null) {
            List<Contatos> todos = contatosRepositorio.findAll();
            List<Contatos> filtrados = new ArrayList<>();
            
            LocalDate inicioBase = LocalDate.of(2024, dataInicio.getMonthValue(), dataInicio.getDayOfMonth());
            LocalDate fimBase = LocalDate.of(2024, dataFim.getMonthValue(), dataFim.getDayOfMonth());
            boolean viraAno = inicioBase.isAfter(fimBase); 

            for(Contatos c : todos) {
                if(c.getDataNascimento() != null && c.getDataNascimento().length() >= 5) {
                    try {
                        int dia = Integer.parseInt(c.getDataNascimento().substring(0, 2));
                        int mes = Integer.parseInt(c.getDataNascimento().substring(3, 5));
                        LocalDate niver = LocalDate.of(2024, mes, dia);
                        
                        if(viraAno) {
                            if(!niver.isBefore(inicioBase) || !niver.isAfter(fimBase)) filtrados.add(c);
                        } else {
                            if(!niver.isBefore(inicioBase) && !niver.isAfter(fimBase)) filtrados.add(c);
                        }
                    } catch(Exception e) {
                        // Ignora datas inválidas
                    }
                }
            }
            mv.addObject("listaContatos", filtrados);
            mv.addObject("filtrado", true); 
        } else {
            mv.addObject("listaContatos", contatosRepositorio.findAll());
        }
        
        return mv;
    }

    // 2. SALVAR NOVO GRUPO (LIMPO - SEM TRY/CATCH)
    @PostMapping("/salvar")
    public String salvar(@Valid Grupo grupo, BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return "grupo/cadastro"; // Certifique-se que a pasta é 'grupos' ou 'grupo' no seu templates
        }
        
        // Se der erro aqui (duplicidade/banco), o GlobalExceptionHandler captura
        grupoService.salvar(grupo);
        
        attributes.addFlashAttribute("mensagem", "Grupo salvo com sucesso!");
        return "redirect:/grupos/gerenciar"; // Ajustei para ir para gerenciar, costuma ser melhor UX
    }

    // 3. TELA DE GERENCIAR
    @GetMapping("/gerenciar")
    public ModelAndView gerenciar(@RequestParam(value = "pesquisa", required = false) String pesquisa) {
        ModelAndView mv = new ModelAndView("grupos/gerenciar");
        
        if (pesquisa != null && !pesquisa.isEmpty()) {
            mv.addObject("listaGrupos", grupoRepositorio.findByNomeContainingIgnoreCaseOrderByNomeAsc(pesquisa));
            mv.addObject("termoPesquisa", pesquisa);
        } else {
            mv.addObject("listaGrupos", grupoRepositorio.findAll(Sort.by(Sort.Direction.ASC, "nome")));
        }
        
        mv.addObject("paginaAtiva", "gerenciarGrupo");
        return mv;
    }
    
    // 4. EXCLUIR GRUPO (LIMPO - SEM TRY/CATCH)
    // Tinha dois métodos excluir, deixei apenas este aqui.
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes attributes) {
        // Se der erro (FK/Contatos vinculados), o GlobalExceptionHandler captura e avisa
        grupoService.excluir(id); // ou grupoService.excluirGrupo(id), dependendo de como está no seu Service
        
        attributes.addFlashAttribute("mensagem", "Grupo excluído com sucesso!");
        return "redirect:/grupos/gerenciar";
    }

    // 5. TELA DE EDIÇÃO
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

    // 6. ATUALIZAR GRUPO (LIMPO - SEM TRY/CATCH)
    @PostMapping("/atualizar")
    public ModelAndView atualizar(Grupo grupo, 
                                  @RequestParam(value = "novosMembros", required = false) List<Long> novosMembros,
                                  RedirectAttributes attributes) {
        
        // Se der erro, o GlobalExceptionHandler assume
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
        
        return new ModelAndView("redirect:/grupos/editar/" + grupo.getId());
    }

    // 7. REMOVER MEMBRO (LIMPO - SEM TRY/CATCH)
    @GetMapping("/removerMembro/{grupoId}/{contatoId}")
    public ModelAndView removerMembro(@PathVariable("grupoId") Long grupoId, 
                                      @PathVariable("contatoId") Long contatoId,
                                      RedirectAttributes attributes) {
        
        Optional<Contatos> contatoOpt = contatosRepositorio.findById(contatoId);
        Optional<Grupo> grupoOpt = grupoRepositorio.findById(grupoId);

        if(contatoOpt.isPresent() && grupoOpt.isPresent()) {
            Contatos contato = contatoOpt.get();
            Grupo grupo = grupoOpt.get();
            
            contato.getGrupos().remove(grupo);
            contatosRepositorio.save(contato);
            
            attributes.addFlashAttribute("mensagemSucesso", "Membro removido do grupo.");
        } else {
            attributes.addFlashAttribute("mensagemErro", "Membro ou Grupo não encontrado.");
        }
        
        return new ModelAndView("redirect:/grupos/editar/" + grupoId);
    }
   
    // 8. DISPARAR AÇÃO (COM UPLOAD DE ARQUIVOS) - Adicionado 'throws Exception'
    @PostMapping("/disparar")
    public ModelAndView dispararAcao(@RequestParam("grupoId") Long grupoId,
                                     @RequestParam("assunto") String assunto,
                                     @RequestParam("mensagem") String mensagem,
                                     @RequestParam(value = "dataAgendamento", required = false) 
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataAgendamento,
                                     @RequestParam(value = "anexos", required = false) MultipartFile[] anexos,
                                     RedirectAttributes attributes) throws Exception { // Exception sobe pro GlobalHandler
        
        Optional<Grupo> grupoOpt = grupoRepositorio.findById(grupoId);
        
        if (grupoOpt.isPresent()) {
            Grupo grupo = grupoOpt.get();
            if (grupo.getContatos().isEmpty()) {
                attributes.addFlashAttribute("mensagemErro", "O grupo está vazio. Adicione contatos antes de enviar.");
            } else {
                
                List<String> arquivosSalvos = new ArrayList<>();
                
                if (anexos != null && anexos.length > 0) {
                    String pastaUpload = "uploads/"; 
                    Files.createDirectories(Paths.get(pastaUpload));

                    for (MultipartFile arquivo : anexos) {
                        if (!arquivo.isEmpty()) {
                            String nomeOriginal = StringUtils.cleanPath(arquivo.getOriginalFilename());
                            String nomeArquivoSalvo = System.currentTimeMillis() + "_" + nomeOriginal;
                            
                            Path caminho = Paths.get(pastaUpload + nomeArquivoSalvo);
                            Files.copy(arquivo.getInputStream(), caminho, StandardCopyOption.REPLACE_EXISTING);
                            
                            arquivosSalvos.add(nomeArquivoSalvo);
                        }
                    }
                }

                emailService.enviarDisparo(grupo.getId(), assunto, mensagem, arquivosSalvos, dataAgendamento);
                
                if (dataAgendamento != null) {
                    attributes.addFlashAttribute("mensagemSucesso", "Envio agendado com sucesso para o grupo " + grupo.getNome());
                } else {
                    attributes.addFlashAttribute("mensagemSucesso", "Envio iniciado para " + grupo.getNome());
                }
            }
        } else {
                attributes.addFlashAttribute("mensagemErro", "Grupo não encontrado.");
        }
        
        return new ModelAndView("redirect:/grupos/gerenciar");
    }

    // 9. DISPARO DIRETO (SEM ANEXOS) - Adicionado 'throws Exception'
    @PostMapping("/disparar-direto")
    public ModelAndView dispararDireto(@RequestParam("idGrupo") Long idGrupo,
                                       @RequestParam("assunto") String assunto,
                                       @RequestParam("conteudo") String conteudo,
                                       RedirectAttributes attributes) throws Exception { // Exception sobe pro GlobalHandler
        
        Optional<Grupo> grupoOpt = grupoRepositorio.findById(idGrupo);
        
        if (grupoOpt.isPresent()) {
            Grupo grupo = grupoOpt.get();
            emailService.enviarDisparo(grupo.getId(), assunto, conteudo, null, null);
            attributes.addFlashAttribute("mensagemSucesso", "Disparo realizado com sucesso para o grupo " + grupo.getNome());
        } else {
            attributes.addFlashAttribute("mensagemErro", "Grupo selecionado não encontrado.");
        }
        
        return new ModelAndView("redirect:/mensagens/caixa/ENVIADAS");
    }
}
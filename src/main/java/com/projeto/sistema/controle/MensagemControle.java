package com.projeto.sistema.controle;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Grupo;
import com.projeto.sistema.modelos.MensagemLog;
import com.projeto.sistema.modelos.UsuarioLogado;
import com.projeto.sistema.repositorios.GrupoRepositorio;
import com.projeto.sistema.repositorios.MensagemLogRepositorio;
import com.projeto.sistema.servicos.EmailService;

@Controller
@RequestMapping("/mensagens")
public class MensagemControle {

    @Autowired
    private MensagemLogRepositorio mensagemRepositorio;

    @Autowired
    private GrupoRepositorio grupoRepositorio;

    @Autowired
    private EmailService emailService;

    @PreAuthorize("hasAuthority('MENSAGEM_VISUALIZAR')")
    @GetMapping("/enviadas")
    public String redirecionarEnviadas() {
        return "redirect:/mensagens/caixa/ENVIADAS";
    }

    @PreAuthorize("hasAuthority('MENSAGEM_VISUALIZAR')")
    @GetMapping("/caixa/{pasta}")
    public ModelAndView listarPorPasta(
            @PathVariable("pasta") String pastaUrl,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "comAnexo", required = false) Boolean comAnexo,
            @RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "data", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {

        ModelAndView mv = new ModelAndView("mensagens/enviadas");
        String pastaNormalizada = pastaUrl.toUpperCase();

        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("dataEnvio").descending());
        LocalDateTime inicio = (data != null) ? data.atStartOfDay() : null;
        LocalDateTime fim = (data != null) ? data.atTime(LocalTime.MAX) : null;

        String filtroPasta = null;
        Boolean filtroFavorito = null;
        Boolean filtroImportante = null;

        switch (pastaNormalizada) {
            case "FAVORITOS": filtroFavorito = true; break;
            case "IMPORTANTE": filtroImportante = true; break;
            case "LIXEIRA": filtroPasta = "LIXEIRA"; break;
            case "TODAS": break;
            default: filtroPasta = pastaNormalizada; break;
        }

        Page<MensagemLog> paginaMensagens;

        if (usuarioLogado.isSuperAdmin()) {
            paginaMensagens = mensagemRepositorio.filtrarMensagensSuperAdmin(
                    filtroPasta, filtroFavorito, filtroImportante, comAnexo, busca, inicio, fim, pageRequest);
        } else {
            paginaMensagens = mensagemRepositorio.filtrarMensagens(
                    filtroPasta, filtroFavorito, filtroImportante, comAnexo, busca, inicio, fim,
                    usuarioLogado.getEmpresa(), pageRequest);
        }

        mv.addObject("listaMensagens", paginaMensagens.getContent());
        mv.addObject("paginaAtual", page);
        mv.addObject("totalPaginas", paginaMensagens.getTotalPages());
        mv.addObject("totalItens", paginaMensagens.getTotalElements());
        mv.addObject("filtroAnexo", comAnexo);
        mv.addObject("termoBusca", busca);
        mv.addObject("filtroData", data);
        mv.addObject("pastaAtiva", pastaNormalizada);

        if (usuarioLogado.isSuperAdmin()) {
            mv.addObject("cntEntrada", 0L);
            mv.addObject("cntEnviadas", 0L);
            mv.addObject("cntFavoritos", 0L);
            mv.addObject("cntImportante", 0L);
            mv.addObject("cntLixeira", 0L);
        } else {
            mv.addObject("cntEntrada", mensagemRepositorio.countByPastaAndLidaFalseAndEmpresa("ENTRADA", usuarioLogado.getEmpresa()));
            mv.addObject("cntEnviadas", mensagemRepositorio.countByPastaAndEmpresa("ENVIADAS", usuarioLogado.getEmpresa()));
            mv.addObject("cntFavoritos", mensagemRepositorio.countByFavoritoTrueAndEmpresa(usuarioLogado.getEmpresa()));
            mv.addObject("cntImportante", mensagemRepositorio.countByImportanteTrueAndEmpresa(usuarioLogado.getEmpresa()));
            mv.addObject("cntLixeira", mensagemRepositorio.countByPastaAndEmpresa("LIXEIRA", usuarioLogado.getEmpresa()));
        }

        return mv;
    }

    @PreAuthorize("hasAuthority('MENSAGEM_VISUALIZAR')")
    @GetMapping("/detalhes/{id}")
    @ResponseBody
    public MensagemLog getDetalhes(@PathVariable Long id, @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
        MensagemLog log = mensagemRepositorio.findById(id).orElse(new MensagemLog());

        if (log.getId() != null && !usuarioLogado.isSuperAdmin()) {
            if (!log.getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId())) {
                return new MensagemLog();
            }
        }

        if (log.getId() != null && !log.isLida()) {
            log.setLida(true);
            mensagemRepositorio.save(log);
        }
        return log;
    }

    @PreAuthorize("hasAuthority('MENSAGEM_VISUALIZAR')")
    @PostMapping("/favoritar/{id}")
    @ResponseBody
    public ResponseEntity<Boolean> toggleFavorito(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
        return mensagemRepositorio.findById(id).map(msg -> {
            if (!usuarioLogado.isSuperAdmin() &&
                    !msg.getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
            msg.setFavorito(!msg.isFavorito());
            mensagemRepositorio.save(msg);
            return ResponseEntity.ok(msg.isFavorito());
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('MENSAGEM_VISUALIZAR')")
    @PostMapping("/importante/{id}")
    @ResponseBody
    public ResponseEntity<Boolean> toggleImportante(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
        return mensagemRepositorio.findById(id).map(msg -> {
            if (!usuarioLogado.isSuperAdmin() &&
                    !msg.getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
            msg.setImportante(!msg.isImportante());
            mensagemRepositorio.save(msg);
            return ResponseEntity.ok(msg.isImportante());
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('MENSAGEM_EXCLUIR')")
    @PostMapping("/lixeira/{id}")
    @ResponseBody
    public ResponseEntity<Void> moverParaLixeira(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
        return mensagemRepositorio.findById(id).map(msg -> {
            if (!usuarioLogado.isSuperAdmin() &&
                    !msg.getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
            msg.setPasta("LIXEIRA");
            mensagemRepositorio.save(msg);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('MENSAGEM_EXCLUIR')")
    @PostMapping("/restaurar/{id}")
    @ResponseBody
    public ResponseEntity<Void> restaurarMensagem(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
        return mensagemRepositorio.findById(id).map(msg -> {
            if (!usuarioLogado.isSuperAdmin() &&
                    !msg.getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
            if (msg.getStatus() == null || msg.getStatus().trim().isEmpty()) {
                msg.setPasta("ENTRADA");
            } else {
                msg.setPasta("ENVIADAS");
            }
            mensagemRepositorio.save(msg);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('MENSAGEM_EXCLUIR')")
    @DeleteMapping("/excluir/{id}")
    @ResponseBody
    public ResponseEntity<Void> excluirPermanente(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
        Optional<MensagemLog> msgOpt = mensagemRepositorio.findById(id);
        if (msgOpt.isPresent()) {
            MensagemLog msg = msgOpt.get();
            if (!usuarioLogado.isSuperAdmin() &&
                    !msg.getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            mensagemRepositorio.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('MENSAGEM_CRIAR')")
    @GetMapping("/preparar-envio/{id}")
    public ModelAndView prepararEnvio(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
        MensagemLog modelo = mensagemRepositorio.findById(id).orElseThrow();

        if (!usuarioLogado.isSuperAdmin() &&
                !modelo.getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId())) {
            return new ModelAndView("redirect:/mensagens/caixa/ENVIADAS");
        }

        ModelAndView mv = new ModelAndView("mensagens/preparar");
        mv.addObject("modelo", modelo);

        if (usuarioLogado.isSuperAdmin()) {
            mv.addObject("listaGrupos", grupoRepositorio.findAll());
        } else {
            mv.addObject("listaGrupos", grupoRepositorio.findByEmpresa(usuarioLogado.getEmpresa()));
        }

        return mv;
    }

    @PreAuthorize("hasAuthority('MENSAGEM_CRIAR')")
    @GetMapping("/preparar-grupo/{idGrupo}")
    public ModelAndView prepararEnvioGrupo(@PathVariable Long idGrupo,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
        ModelAndView mv = new ModelAndView("mensagens/preparar");

        MensagemLog modeloVazio = new MensagemLog();
        modeloVazio.setAssunto("");
        modeloVazio.setConteudo("");

        mv.addObject("modelo", modeloVazio);

        if (usuarioLogado.isSuperAdmin()) {
            mv.addObject("listaGrupos", grupoRepositorio.findAll());
        } else {
            mv.addObject("listaGrupos", grupoRepositorio.findByEmpresa(usuarioLogado.getEmpresa()));
        }

        mv.addObject("idGrupoSelecionado", idGrupo);
        return mv;
    }

    @PreAuthorize("hasAuthority('MENSAGEM_ENVIAR')")
    @PostMapping("/enviar")
    public String enviarMensagem(
            @RequestParam("grupoId") Long grupoId,
            @RequestParam("assunto") String assunto,
            @RequestParam("conteudo") String conteudo,
            RedirectAttributes attributes,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {

        Optional<Grupo> grupoOpt = grupoRepositorio.findById(grupoId);

        if (grupoOpt.isPresent()) {
            Grupo grupo = grupoOpt.get();

            // Tenant Admin verifica posse do grupo
            if (!usuarioLogado.isSuperAdmin() &&
                    !grupo.getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId())) {
                attributes.addFlashAttribute("mensagem", "Erro: Grupo não pertence à sua empresa.");
                attributes.addFlashAttribute("tipoMensagem", "danger");
                return "redirect:/mensagens/caixa/ENVIADAS";
            }

            MensagemLog enviada = new MensagemLog();
            enviada.setEmpresa(grupo.getEmpresa()); // herda empresa do grupo
            enviada.setPasta("ENVIADAS");
            enviada.setNomeGrupoDestino(grupo.getNome());
            enviada.setAssunto(assunto);
            enviada.setConteudo(conteudo);
            enviada.setDataEnvio(LocalDateTime.now());
            enviada.setLida(true);
            enviada.setStatus("SUCESSO");

            if (grupo.getContatos() != null) {
                enviada.setTotalDestinatarios(grupo.getContatos().size());
            }

            mensagemRepositorio.save(enviada);
            attributes.addFlashAttribute("mensagem", "Mensagem enviada com sucesso para " + grupo.getNome() + "!");
            attributes.addFlashAttribute("tipoMensagem", "success");
        } else {
            attributes.addFlashAttribute("mensagem", "Erro: Grupo selecionado não existe.");
            attributes.addFlashAttribute("tipoMensagem", "danger");
        }

        return "redirect:/mensagens/caixa/ENVIADAS";
    }

    @PreAuthorize("hasAuthority('MENSAGEM_CRIAR')")
    @PostMapping("/salvarModelo")
    public String salvarModelo(
            @RequestParam("categoria") String categoria,
            @RequestParam("assunto") String assunto,
            @RequestParam("conteudo") String conteudo,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {

        MensagemLog modelo = new MensagemLog();
        modelo.setEmpresa(usuarioLogado.getEmpresa());
        modelo.setPasta("ENTRADA");
        modelo.setNomeGrupoDestino(categoria.toUpperCase());
        modelo.setAssunto(assunto);
        modelo.setConteudo(conteudo);
        modelo.setDataEnvio(LocalDateTime.now());
        modelo.setLida(true);
        mensagemRepositorio.save(modelo);

        return "redirect:/mensagens/caixa/ENTRADA";
    }

    @PreAuthorize("hasAuthority('MENSAGEM_CRIAR')")
    @PostMapping("/agendar-modelo")
    public String agendarModelo(@RequestParam("assunto") String assunto,
            @RequestParam("conteudo") String conteudo) {
        String tituloCodificado = assunto;
        try {
            tituloCodificado = URLEncoder.encode(assunto, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/administrativo/agenda?acao=novoEvento&titulo=" + tituloCodificado;
    }

    @PreAuthorize("hasAuthority('MENSAGEM_VISUALIZAR')")
    @PostMapping("/salvarObservacao")
    @ResponseBody
    public ResponseEntity<?> salvarObservacao(@RequestParam Long id, @RequestParam String observacao,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
        try {
            MensagemLog mensagem = mensagemRepositorio.findById(id).orElse(null);

            if (mensagem != null) {
                if (!usuarioLogado.isSuperAdmin() &&
                        !mensagem.getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sem permissão.");
                }
                mensagem.setObservacao(observacao);
                mensagemRepositorio.save(mensagem);
                return ResponseEntity.ok("Salvo com sucesso!");
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Mensagem não encontrada.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar.");
        }
    }
}
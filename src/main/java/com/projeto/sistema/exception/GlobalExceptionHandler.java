package com.projeto.sistema.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest; 

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. TRATA O ERRO DE EXCLUSÃO (FK) E DUPLICIDADE
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolationException(DataIntegrityViolationException e, RedirectAttributes attributes, HttpServletRequest request) {
        
        String erroTecnico = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
        
        if (erroTecnico != null && erroTecnico.toLowerCase().contains("foreign key")) {
             attributes.addFlashAttribute("erro", "Não é possível excluir este registro pois ele possui dependências vinculadas (Ex: contatos, usuários).");
        } 
        else if (erroTecnico != null && (erroTecnico.toLowerCase().contains("duplicate") || erroTecnico.toLowerCase().contains("unique"))) {
            attributes.addFlashAttribute("erro", "Já existe um registro com este nome ou documento único.");
        } 
        else {
            attributes.addFlashAttribute("erro", "Erro de integridade: " + erroTecnico);
        }

        // CORREÇÃO: Descobre de qual página o usuário veio, para devolvê-lo à mesma página com o erro
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/"); // Se não souber de onde veio, manda para o Início (/)
    }

    // 2. TRATA O ERRO "COULD NOT COMMIT JPA TRANSACTION" (O erro da tela branca)
    @ExceptionHandler(TransactionSystemException.class)
    public String handleTransactionSystemException(TransactionSystemException e, RedirectAttributes attributes, HttpServletRequest request) {
        Throwable rootCause = e.getRootCause();
        String mensagem = "Erro ao processar a transação.";
        
        if (rootCause != null) {
            mensagem += " Detalhe: " + rootCause.getMessage();
        }
        
        attributes.addFlashAttribute("erro", mensagem);
        
        // CORREÇÃO: Devolve à página de origem
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    // 3. TRATA QUALQUER OUTRO ERRO GENÉRICO (Fallback)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception e, HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("error"); 
        mv.addObject("status", 500);
        mv.addObject("error", "Erro Interno");
        mv.addObject("message", e.getMessage());
        return mv;
    }

    // 4. TRATA ERRO DE ACESSO NEGADO (SPRING SECURITY)
    @org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public String handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        // Redireciona diretamente para a nossa tela nova de bloqueio
        return "error/403";
    }
}
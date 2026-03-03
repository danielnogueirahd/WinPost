package com.projeto.sistema.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException; // <--- IMPORTANTE
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest; 

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. TRATA O ERRO DE EXCLUSÃO (FK) E DUPLICIDADE
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolationException(DataIntegrityViolationException e, RedirectAttributes attributes) {
        
        String erroTecnico = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
        
        if (erroTecnico != null && erroTecnico.toLowerCase().contains("foreign key")) {
             attributes.addFlashAttribute("erro", "Não é possível excluir este grupo pois ele possui contatos vinculados. Remova os contatos primeiro.");
        } 
        else if (erroTecnico != null && (erroTecnico.toLowerCase().contains("duplicate") || erroTecnico.toLowerCase().contains("unique"))) {
            attributes.addFlashAttribute("erro", "Já existe um registro com este nome.");
        } 
        else {
            attributes.addFlashAttribute("erro", "Erro de integridade: " + erroTecnico);
        }

        return "redirect:/grupos/gerenciar";
    }

    // 2. TRATA O ERRO "COULD NOT COMMIT JPA TRANSACTION" (O erro da tela branca)
    @ExceptionHandler(TransactionSystemException.class)
    public String handleTransactionSystemException(TransactionSystemException e, RedirectAttributes attributes) {
        Throwable rootCause = e.getRootCause();
        String mensagem = "Erro ao processar a transação.";
        
        if (rootCause != null) {
            // Tenta pegar a mensagem real escondida na transação
            mensagem += " Detalhe: " + rootCause.getMessage();
        }
        
        attributes.addFlashAttribute("erro", mensagem);
        return "redirect:/grupos/gerenciar";
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
}
package com.WinPost-main.exception; // <--- CONFIRA SE ESSE É O SEU PACOTE BASE CORRETO

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest; // Se usar Spring Boot 3+, troque javax por jakarta

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. TRATA O ERRO DE EXCLUSÃO (FK) E DUPLICIDADE
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolationException(DataIntegrityViolationException e, RedirectAttributes attributes) {
        // Esse erro acontece quando tenta apagar um grupo que tem contatos
        // OU quando tenta criar um grupo com nome duplicado (se tiver unique no banco)
        
        String erroTecnico = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
        
        // Verifica se é erro de exclusão (Foreign Key)
        if (erroTecnico != null && erroTecnico.toLowerCase().contains("foreign key")) {
            attributes.addFlashAttribute("erro", "Não é possível excluir este grupo pois ele possui contatos vinculados. Remova os contatos primeiro.");
        } 
        // Verifica se é erro de duplicidade (Duplicate entry)
        else if (erroTecnico != null && erroTecnico.toLowerCase().contains("duplicate")) {
            attributes.addFlashAttribute("erro", "Já existe um grupo com este nome. Por favor, escolha outro.");
        } 
        else {
            attributes.addFlashAttribute("erro", "Erro de integridade de dados: " + erroTecnico);
        }

        return "redirect:/grupos"; // <--- Voltar para a listagem de grupos
    }

    // 2. TRATA QUALQUER OUTRO ERRO GENÉRICO
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception e, HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("error"); // <--- Nome da sua página de erro html (se tiver)
        mv.addObject("status", 500);
        mv.addObject("error", "Erro Interno");
        mv.addObject("message", e.getMessage());
        return mv;
    }
}
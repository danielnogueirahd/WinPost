package com.projeto.sistema.controle;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.projeto.sistema.servicos.GrupoService;

@Controller
@RequestMapping("/grupos")
public class GrupoControle {

    @Autowired
    private GrupoService grupoService;

    // Endpoint que recebe o POST do JavaScript
    @PostMapping("/criar")
    @ResponseBody // Indica que a resposta é um dado puro (texto/JSON), não uma página HTML
    public ResponseEntity<String> criarGrupo(@RequestParam("nome") String nome, 
                                             @RequestParam("ids[]") List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body("Nenhum contato selecionado.");
            }
            
            grupoService.criarGrupoComContatos(nome, ids);
            return ResponseEntity.ok("Grupo '" + nome + "' criado com sucesso!");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erro ao criar grupo: " + e.getMessage());
        }
    }
}
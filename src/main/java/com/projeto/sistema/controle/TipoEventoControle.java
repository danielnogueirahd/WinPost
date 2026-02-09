package com.projeto.sistema.controle;

import com.projeto.sistema.modelos.TipoEvento;
import com.projeto.sistema.repositorios.TipoEventoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-evento")
public class TipoEventoControle {

    @Autowired
    private TipoEventoRepositorio repositorio;

    @GetMapping
    public List<TipoEvento> listar() {
        return repositorio.findAll();
    }

    @PostMapping
    public TipoEvento criar(@RequestBody TipoEvento novoTipo) {
        return repositorio.save(novoTipo);
    }
}
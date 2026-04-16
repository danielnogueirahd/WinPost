package com.projeto.sistema.servicos;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.projeto.sistema.modelos.Empresa;
import com.projeto.sistema.repositorios.EmpresaRepositorio;

@Service
@Transactional
public class EmpresaService {

    @Autowired
    private EmpresaRepositorio empresaRepositorio;

    public Empresa salvar(Empresa empresa) {
        return empresaRepositorio.save(empresa);
    }

    public List<Empresa> listarTodas() {
        return empresaRepositorio.findAll();
    }
    
    // Métodos buscarPorId e excluir...
}
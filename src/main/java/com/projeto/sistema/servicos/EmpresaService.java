package com.projeto.sistema.servicos;

import java.util.List;
import java.util.Optional;

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
        // CORREÇÃO: Se o CNPJ vier vazio do formulário, transformamos em nulo.
        // O SQL Server permite vários valores "null", mas bloqueia vários valores vazios ("").
        if (empresa.getCnpj() != null && empresa.getCnpj().trim().isEmpty()) {
            empresa.setCnpj(null);
        }
        
        return empresaRepositorio.save(empresa);
    }

    public List<Empresa> listarTodas() {
        return empresaRepositorio.findAll();
    }
    
    public Empresa buscarPorId(Long id) {
        Optional<Empresa> empresa = empresaRepositorio.findById(id);
        return empresa.orElse(null);
    }

    public void excluir(Long id) {
        empresaRepositorio.deleteById(id);
    }
}
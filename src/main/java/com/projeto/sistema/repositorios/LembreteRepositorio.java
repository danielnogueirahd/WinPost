package com.projeto.sistema.repositorios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projeto.sistema.modelos.Empresa;
import com.projeto.sistema.modelos.Lembrete;

@Repository
public interface LembreteRepositorio extends JpaRepository<Lembrete, Long> {

    // Busca lembretes do período filtrado por empresa
    List<Lembrete> findByDataHoraBetweenAndEmpresa(LocalDateTime inicio, LocalDateTime fim, Empresa empresa);

    // Busca todos os lembretes da empresa
    List<Lembrete> findByEmpresa(Empresa empresa);
}
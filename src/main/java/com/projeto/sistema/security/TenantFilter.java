package com.projeto.sistema.security;

import com.projeto.sistema.modelos.UsuarioLogado;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof UsuarioLogado) {
                UsuarioLogado usuarioLogado = (UsuarioLogado) authentication.getPrincipal();
                TenantContext.setCurrentTenant(usuarioLogado.getEmpresa());
            } else {
                TenantContext.clear();
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
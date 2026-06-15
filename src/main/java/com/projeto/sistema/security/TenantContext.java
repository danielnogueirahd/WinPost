package com.projeto.sistema.security;

import com.projeto.sistema.modelos.Empresa;

public class TenantContext {

    private static final ThreadLocal<Empresa> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(Empresa empresa) {
        currentTenant.set(empresa);
    }

    public static Empresa getCurrentTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
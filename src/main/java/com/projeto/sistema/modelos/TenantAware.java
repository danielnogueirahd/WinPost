package com.projeto.sistema.modelos;

/**
 * Interface de contrato para todas as entidades Multi-Tenant.
 * Entidades que implementam esta interface são automaticamente
 * filtradas por empresa em nível de serviço.
 */
public interface TenantAware {
    Empresa getEmpresa();
    void setEmpresa(Empresa empresa);
}
package com.projeto.sistema.modelos;
public enum Permissao {
    
    // Módulo: Usuários
    USUARIO_VISUALIZAR("Usuários", "Visualizar", "Permite acessar a lista de usuários da empresa."),
    USUARIO_CRIAR("Usuários", "Criar", "Permite cadastrar novos funcionários."),
    USUARIO_EDITAR("Usuários", "Editar", "Permite alterar dados de usuários existentes."),
    USUARIO_EXCLUIR("Usuários", "Excluir", "Permite remover o acesso de usuários."),

    // Módulo: Contatos
    CONTATO_VISUALIZAR("Contatos", "Visualizar", "Permite ver a carteira de contatos/clientes."),
    CONTATO_CRIAR("Contatos", "Criar", "Permite adicionar novos contatos ao sistema."),
    CONTATO_EDITAR("Contatos", "Editar", "Permite atualizar informações dos contatos."),
    CONTATO_EXCLUIR("Contatos", "Excluir", "Permite apagar contatos da base."),

    // Módulo: Grupos
    GRUPO_VISUALIZAR("Grupos", "Visualizar", "Permite ver os grupos de contatos criados."),
    GRUPO_CRIAR("Grupos", "Criar", "Permite criar novos agrupamentos de contatos."),
    GRUPO_EDITAR("Grupos", "Editar", "Permite alterar o nome ou membros do grupo."),
    GRUPO_EXCLUIR("Grupos", "Excluir", "Permite deletar grupos do sistema."),

    // Módulo: Mensagens
    MENSAGEM_VISUALIZAR("Mensagens", "Visualizar Histórico", "Permite ver as mensagens enviadas no log."),
    MENSAGEM_ENVIAR("Mensagens", "Enviar Mensagens", "Permite disparar mensagens para contatos ou grupos."),
    MENSAGEM_CRIAR("Mensagens", "Criar Modelos", "Permite criar templates/modelos de mensagens."),
    MENSAGEM_EXCLUIR("Mensagens", "Excluir", "Permite apagar registros de mensagens."),

    // Módulo: Agenda
    AGENDA_VISUALIZAR("Agenda", "Visualizar", "Permite ver os eventos e lembretes."),
    AGENDA_CRIAR("Agenda", "Criar Eventos", "Permite agendar novos compromissos."),
    AGENDA_EDITAR("Agenda", "Editar Eventos", "Permite reagendar ou alterar detalhes."),
    AGENDA_EXCLUIR("Agenda", "Excluir Eventos", "Permite cancelar compromissos."),

    // Módulo: Relatórios
    RELATORIO_VISUALIZAR("Relatórios", "Visualizar", "Permite acessar o painel de métricas."),
    RELATORIO_GERAR("Relatórios", "Gerar/Exportar", "Permite emitir e baixar relatórios em PDF/Excel."),

    // Módulo: Configurações
    CONFIGURACOES_SISTEMA("Configurações", "Acesso Total", "Acesso exclusivo para gerenciar empresas e parâmetros globais.");

    // Variáveis que vão guardar os textos amigáveis
    private final String modulo;
    private final String nomeAmigavel;
    private final String descricao;

    // Construtor do Enum
    Permissao(String modulo, String nomeAmigavel, String descricao) {
        this.modulo = modulo;
        this.nomeAmigavel = nomeAmigavel;
        this.descricao = descricao;
    }

    // Getters para usarmos no HTML
    public String getModulo() { return modulo; }
    public String getNomeAmigavel() { return nomeAmigavel; }
    public String getDescricao() { return descricao; }
}
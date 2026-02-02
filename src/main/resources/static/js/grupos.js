$(document).ready(function() {

    // ==========================================================================
    // 1. CONFIGURAÇÃO DO SELECT2 (SELEÇÃO DE MEMBROS)
    // ==========================================================================
    function formatarContatoOpcao(state) {
        if (!state.id) { return state.text; }
        var nome = $(state.element).data('nome') || state.text;
        var email = $(state.element).data('email') || '';
        var inicial = nome.charAt(0).toUpperCase();

        return $(
            '<div class="select2-item-contato">' +
                '<div class="avatar-mini">' + inicial + '</div>' +
                '<div>' +
                    '<div class="fw-bold text-dark" style="font-size: 0.95rem;">' + nome + '</div>' +
                    '<div class="text-muted small" style="font-size: 0.8rem;">' + email + '</div>' +
                '</div>' +
            '</div>'
        );
    }

    function formatarContatoSelecao(state) {
        if (!state.id) { return state.text; }
        var nome = $(state.element).data('nome') || state.text;
        return $('<span><i class="fa-solid fa-user me-1 text-muted small"></i> ' + nome + '</span>');
    }

    $('.select2-multiple').select2({
        theme: 'bootstrap-5',
        placeholder: "Pesquise por nome ou email...",
        allowClear: true,
        width: '100%',
        templateResult: formatarContatoOpcao,
        templateSelection: formatarContatoSelecao,
        escapeMarkup: function(m) { return m; }
    });


    // ==========================================================================
    // 2. SELEÇÃO DE CONTATOS (CORREÇÃO DO "SELECIONAR TODOS")
    // ==========================================================================

    // Memória para guardar os IDs (Set garante que não haja duplicatas)
    var idsSelecionados = new Set();

    // Inicializa a Tabela
    var table = $('#tabelaSelecao').DataTable({
        language: {
            url: '//cdn.datatables.net/plug-ins/1.13.7/i18n/pt-BR.json',
            search: "",
            searchPlaceholder: "Buscar..."
        },
        pageLength: 9,
        lengthChange: false,
        ordering: false, // Desabilitado para simplificar a seleção
        dom: "<'row mb-3'<'col-sm-12'f>><'row'<'col-sm-12'tr>><'row mt-3'<'col-sm-12'p>>"
    });

    // Ajuste visual do campo de busca
    $('.dataTables_filter input').addClass('form-control ps-3').css('border-radius', '8px');


    // --- FUNÇÕES DE SINCRONIZAÇÃO ---

    function atualizarContador() {
        var count = idsSelecionados.size;
        var texto = count === 0 ? '0 contatos selecionados' :
                    count === 1 ? '1 contato selecionado' : count + ' contatos selecionados';
        
        $('#contadorSelecao').text(texto);
        
        if (count > 0) $('#contadorSelecao').addClass('text-primary').removeClass('text-muted');
        else $('#contadorSelecao').addClass('text-muted').removeClass('text-primary');
    }

    function sincronizarVisual() {
        // 1. Percorre TODOS os checkboxes que o DataTables gerencia (visíveis ou não)
        // table.$ acessa o jQuery de todas as linhas da tabela virtualmente
        table.$('.check-item').each(function() {
            var id = $(this).val();
            var deveEstarMarcado = idsSelecionados.has(id);
            
            // Marca/Desmarca o checkbox
            $(this).prop('checked', deveEstarMarcado);
            
            // Adiciona classe visual na linha (apenas se a linha estiver no DOM visível)
            var tr = $(this).closest('tr');
            if (tr.length > 0) {
                if (deveEstarMarcado) tr.addClass('row-selected');
                else tr.removeClass('row-selected');
            }
        });

        // 2. Atualiza o checkbox mestre "Selecionar Todos" (#checkAll)
        // Verifica apenas os itens da PÁGINA ATUAL para decidir se marca o mestre
        var checkboxesPagina = $('#tabelaSelecao tbody input.check-item');
        var totalPagina = checkboxesPagina.length;
        var marcadosPagina = checkboxesPagina.filter(':checked').length;
        
        // Se houver itens na página e todos estiverem marcados -> Marca o CheckAll
        var todosMarcados = (totalPagina > 0 && totalPagina === marcadosPagina);
        $('#checkAll').prop('checked', todosMarcados);
    }


    // --- EVENTOS ---

    // 1. Clique no "SELECIONAR TODOS" (Cabeçalho)
    $('#tabelaSelecao').on('click', '#checkAll', function() {
        var isChecked = this.checked;

        // Pega todas as linhas filtradas atualmente (se houver busca, pega só o resultado da busca)
        var rows = table.rows({ 'search': 'applied' }).nodes();
        
        // Procura os checkboxes dentro dessas linhas
        var checkboxes = $(rows).find('.check-item');

        checkboxes.each(function() {
            var id = $(this).val();
            if (isChecked) {
                idsSelecionados.add(id);
            } else {
                idsSelecionados.delete(id);
            }
        });

        sincronizarVisual();
        atualizarContador();
    });

    // 2. Clique em um checkbox individual
    // Usamos 'change' delegado ao tbody para pegar itens mesmo após paginação
    $('#tabelaSelecao tbody').on('change', '.check-item', function() {
        var id = $(this).val();
        if (this.checked) {
            idsSelecionados.add(id);
        } else {
            idsSelecionados.delete(id);
        }
        sincronizarVisual();
        atualizarContador();
    });

    // 3. Clique na Linha (UX) - Marca ao clicar em qualquer lugar da linha
    $('#tabelaSelecao tbody').on('click', 'tr', function(e) {
        // Evita loop se clicar direto no checkbox
        if (e.target.type !== 'checkbox') {
            var checkbox = $(this).find('.check-item');
            checkbox.prop('checked', !checkbox.is(':checked')).trigger('change');
        }
    });

    // 4. Ao Redesenhar a Tabela (mudar de página, ordenar, filtrar)
    table.on('draw', function() {
        sincronizarVisual();
    });


    // --- INICIALIZAÇÃO ---
    // Recupera estados se houver (ex: erro de validação do servidor)
    table.$('.check-item').each(function() {
        if (this.checked) idsSelecionados.add($(this).val());
    });
    atualizarContador();
    sincronizarVisual();


    // ==========================================================================
    // 3. ENVIO DO FORMULÁRIO
    // ==========================================================================
    $('#formGrupo').on('submit', function(e) {
        var form = this;

        // 1. Desabilita checkboxes originais (para não enviar nada da tabela visual)
        // Isso evita envio parcial apenas da página 1
        table.$('.check-item').prop('disabled', true);

        // 2. Cria inputs hidden para CADA ID selecionado na memória
        // Isso garante o envio completo
        idsSelecionados.forEach(function(id) {
            $(form).append(
                $('<input>')
                    .attr('type', 'hidden')
                    .attr('name', 'idsContatos') // Nome do campo esperado no Controller
                    .attr('value', id)
            );
        });
        
        // Envia o formulário
    });

}); // Fim do Ready

// ==========================================================================
// 4. FUNÇÕES GLOBAIS
// ==========================================================================
function prepararFiltro() {
    var nome = document.getElementById('inputNome') ? document.getElementById('inputNome').value : '';
    var desc = document.getElementById('inputDescricao') ? document.getElementById('inputDescricao').value : '';

    if(document.getElementById('hiddenNome')) document.getElementById('hiddenNome').value = nome;
    if(document.getElementById('hiddenDescricao')) document.getElementById('hiddenDescricao').value = desc;
};
$(document).ready(function() {

    // ==========================================================================
    // 1. CONFIGURAÇÃO DO SELECT2 (SELEÇÃO DE MEMBROS)
    // ==========================================================================

    // Função para formatar como a opção aparece na LISTA (Dropdown aberto)
    function formatarContatoOpcao(state) {
        if (!state.id) { return state.text; }

        // Pega os dados que colocamos no HTML (th:data-nome, etc)
        var nome = $(state.element).data('nome') || state.text;
        var email = $(state.element).data('email') || '';
        var inicial = nome.charAt(0).toUpperCase();

        var $state = $(
            '<div class="select2-item-contato">' +
                '<div class="avatar-mini">' + inicial + '</div>' +
                '<div>' +
                    '<div class="fw-bold text-dark" style="font-size: 0.95rem;">' + nome + '</div>' +
                    '<div class="text-muted small" style="font-size: 0.8rem;">' + email + '</div>' +
                '</div>' +
            '</div>'
        );
        return $state;
    }

    // Função para formatar como a opção aparece SELECIONADA (No input fechado)
    function formatarContatoSelecao(state) {
        if (!state.id) { return state.text; }
        var nome = $(state.element).data('nome') || state.text;
        
        // Na seleção, mostramos apenas o nome para economizar espaço
        return $('<span><i class="fa-solid fa-user me-1 text-muted small"></i> ' + nome + '</span>');
    }

    // Inicialização do Select2 com as funções customizadas
    $('.select2-multiple').select2({
        theme: 'bootstrap-5',
        placeholder: "Pesquise por nome ou email...",
        allowClear: true,
        width: '100%',
        templateResult: formatarContatoOpcao,      // Usa o layout rico na lista
        templateSelection: formatarContatoSelecao,  // Usa o layout simples na seleção
        escapeMarkup: function(m) { return m; }     // Permite HTML dentro das opções
    });


    // ==========================================================================
    // 2. CONFIGURAÇÃO DO DATATABLES (TABELA DE GRUPOS)
    // ==========================================================================

    var table = $('#tabelaSelecao').DataTable({
        language: {
            url: '//cdn.datatables.net/plug-ins/1.13.7/i18n/pt-BR.json',
            search: "",
            searchPlaceholder: "Buscar..."
        },
        pageLength: 9,
        lengthChange: false,
        dom: "<'row mb-3'<'col-sm-12'f>><'row'<'col-sm-12'tr>><'row mt-3'<'col-sm-12'p>>",
        columnDefs: [{ orderable: false, targets: 0 }]
    });

    // Estilização do campo de busca gerado dinamicamente
    $('.dataTables_filter input').addClass('form-control ps-3').css('border-radius', '8px');

    // Função de atualização visual da tabela
    function atualizarInterface() {
        var selecionados = 0;
        table.rows().nodes().to$().each(function() {
            var row = $(this);
            if (row.find('.check-item').is(':checked')) {
                row.addClass('row-selected');
                selecionados++;
            } else {
                row.removeClass('row-selected');
            }
        });

        var texto = selecionados === 0 ? '0 contatos selecionados' :
            selecionados === 1 ? '1 contato selecionado' : selecionados + ' contatos selecionados';
        $('#contadorSelecao').text(texto);

        if (selecionados > 0) $('#contadorSelecao').addClass('text-primary').removeClass('text-muted');
        else $('#contadorSelecao').addClass('text-muted').removeClass('text-primary');
    }

    // Eventos da Tabela
    $('#tabelaSelecao').on('change', '.check-item', function() {
        atualizarInterface();
        if (!$(this).is(':checked')) $('#checkAll').prop('checked', false);
    });

    $('#tabelaSelecao').on('click', 'tbody tr', function(e) {
        if (e.target.type !== 'checkbox') {
            var checkbox = $(this).find('.check-item');
            checkbox.prop('checked', !checkbox.is(':checked')).trigger('change');
        }
    });

    $('#checkAll').on('click', function() {
        var isChecked = this.checked;
        var rows = table.rows({ 'search': 'applied' }).nodes();
        $('input[type="checkbox"]', rows).prop('checked', isChecked);
        atualizarInterface();
    });

}); // Fim do $(document).ready


// ==========================================================================
// 3. FUNÇÕES GLOBAIS (FORA DO DOCUMENT READY)
// ==========================================================================

// Função do Sidebar
window.toggleMenu = function(menuId, iconId) {
    var menu = document.getElementById(menuId);
    var icon = document.getElementById(iconId);
    if (menu) {
        if (menu.classList.contains('d-none')) {
            menu.classList.remove('d-none');
            if (icon) { icon.classList.remove('fa-chevron-down'); icon.classList.add('fa-chevron-up'); }
        } else {
            menu.classList.add('d-none');
            if (icon) { icon.classList.remove('fa-chevron-up'); icon.classList.add('fa-chevron-down'); }
        }
    }
};

// Função para preparar filtros (se usada em outras telas)
function prepararFiltro() {
    var nomeDigitado = document.getElementById('inputNome').value;
    var descDigitada = document.getElementById('inputDescricao').value;

    if(document.getElementById('hiddenNome')) 
        document.getElementById('hiddenNome').value = nomeDigitado;
    
    if(document.getElementById('hiddenDescricao'))
        document.getElementById('hiddenDescricao').value = descDigitada;
};
// ... (código existente do Select2 e Datatables) ...

    // --- NOVA LÓGICA: Carregar Template Automaticamente ---
    const params = new URLSearchParams(window.location.search);
    if (params.get('acao') === 'usarTemplate') {
        let idTemplate = params.get('id');
        
        // Busca o conteúdo do modelo via AJAX
        $.get('/mensagens/detalhes/' + idTemplate, function(data) {
            // Abre o Modal de Disparo
            var modalDisparo = new bootstrap.Modal(document.getElementById('modalDisparo'));
            modalDisparo.show();
            
            // Preenche os campos
            $('input[name="assunto"]').val(data.assunto);
            $('#summernote').summernote('code', data.conteudo);
            
            // Limpa a URL para não reabrir ao atualizar a página
            window.history.replaceState({}, document.title, window.location.pathname);
        });
    }
// Fim do $(document).ready
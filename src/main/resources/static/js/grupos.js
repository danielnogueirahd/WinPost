$(document).ready(function() {

    // ==========================================================================
    // 1. SELECT2 (Formatação da busca de membros)
    // ==========================================================================
    function formatarContatoOpcao(state) {
        if (!state.id) return state.text;
        var nome = $(state.element).data('nome') || state.text;
        var email = $(state.element).data('email') || '';
        return $('<div class="select2-item-contato"><div class="fw-bold">' + nome + '</div><div class="text-muted small">' + email + '</div></div>');
    }
    
    if($('.select2-multiple').length) {
        $('.select2-multiple').select2({
            theme: 'bootstrap-5',
            placeholder: "Pesquise por nome...",
            width: '100%',
            templateResult: formatarContatoOpcao,
            escapeMarkup: function(m) { return m; }
        });
    }

    // ==========================================================================
    // 2. LÓGICA DA TABELA E SELEÇÃO (CORRIGIDA)
    // ==========================================================================
    
    var idsSelecionados = new Set();
    
    var table = $('#tabelaSelecao').DataTable({
        language: {
            url: '//cdn.datatables.net/plug-ins/1.13.7/i18n/pt-BR.json',
            searchPlaceholder: "Buscar..."
        },
        pageLength: 9,
        lengthChange: false,
        ordering: false,
        dom: "<'row mb-3'<'col-sm-12'f>><'row'<'col-sm-12'tr>><'row mt-3'<'col-sm-12'p>>"
    });

    $('.dataTables_filter input').addClass('form-control ps-3').css('border-radius', '8px');

    // --- FUNÇÃO CENTRAL: ALTERAR TODOS ---
    function toggleTodos(marcar) {
        // Pega todos os NÓS do DOM da tabela filtrada (independente da página)
        var nodesFiltrados = table.rows({ search: 'applied' }).nodes();
        
        // Itera sobre os checkboxes reais em vez de usar Regex
        $(nodesFiltrados).find('.check-item').each(function() {
            var id = $(this).val();
            if (marcar) {
                idsSelecionados.add(id);
            } else {
                idsSelecionados.delete(id);
            }
        });

        atualizarVisual();
    }

    // --- FUNÇÃO CENTRAL: ATUALIZAR VISUAL ---
	function atualizarVisual() {
	        // Atualiza linhas visíveis
	        $(table.rows({ page: 'current' }).nodes()).each(function() {
	            var input = $(this).find('.check-item');
	            var id = input.val();
	            
	            if (idsSelecionados.has(id)) {
	                input.prop('checked', true);
	            } else {
	                input.prop('checked', false);
	            }
	        });
	        // ... resto do código continua igual abaixo ...

        // 2. Atualiza contador e badge de status
        var total = idsSelecionados.size;
        var textoContador = total === 1 ? '1 contato selecionado' : total + ' contatos selecionados';
        
        $('#contadorSelecao').text(textoContador);
        if(total > 0) $('#contadorSelecao').removeClass('text-muted').addClass('text-primary');
        else $('#contadorSelecao').addClass('text-muted').removeClass('text-primary');

        // 3. Verifica se TUDO na PÁGINA ATUAL está marcado para o checkbox do cabeçalho
        var inputsPagina = $('.check-item', table.rows({ page: 'current' }).nodes());
        var totalPagina = inputsPagina.length;
        var marcadosPagina = inputsPagina.filter(':checked').length;
        var paginaCheia = (totalPagina > 0 && totalPagina === marcadosPagina);
        
        $('#checkAll').prop('checked', paginaCheia);

        // 4. Verifica se TUDO NA TABELA FILTRADA (todas as páginas) está marcado para o BOTÃO GLOBAL
        var totalFiltrados = table.rows({ search: 'applied' }).count();
        var todosSelecionadosGeral = (total > 0 && total === totalFiltrados);

        var btnAll = $('#btnToggleAll');
        var iconeBtnAll = btnAll.find('i');
        var txtBtnAll = $('#txtBtnAll');

        if (todosSelecionadosGeral) {
            // Estado: Desmarcar Todos
            txtBtnAll.text('Desmarcar Todos');
            btnAll.removeClass('btn-light border').addClass('btn-danger text-white border-danger');
            iconeBtnAll.removeClass('text-primary fa-check-double').addClass('text-white fa-xmark');
        } else {
            // Estado: Selecionar Todos
            txtBtnAll.text('Selecionar Todos');
            btnAll.removeClass('btn-danger text-white border-danger').addClass('btn-light border');
            iconeBtnAll.removeClass('text-white fa-xmark').addClass('text-primary fa-check-double');
        }
    }


    // --- EVENTOS ---

    // 1. Clique no Checkbox Mestre da Tabela (#checkAll)
    $('#tabelaSelecao').on('click', '#checkAll', function() {
        var marcar = this.checked;
        toggleTodos(marcar);
    });

    // 2. Clique no Botão Grande (#btnToggleAll)
    $('#btnToggleAll').on('click', function(e) {
        e.preventDefault();
        
        // Avalia se vamos marcar ou desmarcar com base no todo, não só na página
        var totalFiltrados = table.rows({ search: 'applied' }).count();
        var todosSelecionadosGeral = (idsSelecionados.size > 0 && idsSelecionados.size === totalFiltrados);
        
        var novaAcao = !todosSelecionadosGeral; // Inverte o estado atual
        toggleTodos(novaAcao);
    });

    // 3. Clique em Checkbox Individual (.check-item)
    $('#tabelaSelecao tbody').on('click', '.check-item', function(e) {
        e.stopPropagation(); 
        var id = $(this).val();
        if (this.checked) idsSelecionados.add(id);
        else idsSelecionados.delete(id);
        atualizarVisual();
    });

    // 4. Clique na Linha (Melhoria de UX)
    $('#tabelaSelecao tbody').on('click', 'tr', function(e) {
        if (e.target.type !== 'checkbox') {
            var checkbox = $(this).find('.check-item');
            if (checkbox.length) {
                 var novoEstado = !checkbox.prop('checked');
                 checkbox.prop('checked', novoEstado); 
                 
                 var id = checkbox.val();
                 if (novoEstado) idsSelecionados.add(id);
                 else idsSelecionados.delete(id);
                 
                 atualizarVisual();
            }
        }
    });

    // 5. Redesenho da Tabela (Mantém a seleção ao mudar de página)
    table.on('draw', function() {
        atualizarVisual();
    });

    // --- CARREGAMENTO INICIAL ---
    $('.check-item:checked').each(function() {
        idsSelecionados.add($(this).val());
    });
    atualizarVisual();

    // ==========================================================================
    // 3. SUBMISSÃO DO FORMULÁRIO
    // ==========================================================================
    $('#formGrupo').on('submit', function(e) {
        var form = this;
        
        // Limpa inputs hidden antigos para evitar duplicação ou conflitos
        $('#idsContatos').remove();
        $(form).find('input[name="idsContatos"]').remove();

        if (idsSelecionados.size === 0) {
            e.preventDefault();
            alert('Por favor, selecione pelo menos um contato para criar o grupo.');
            return false;
        }

        // Transforma o Set de volta em um formato suportado pelo seu Controller (Vírgula)
        var idsString = Array.from(idsSelecionados).join(',');
        
        $(form).append(
            $('<input>')
                .attr('type', 'hidden')
                .attr('name', 'idsContatos')
                .attr('value', idsString)
        );
    });

});

function prepararFiltro() {
    var n = document.getElementById('inputNome');
    var d = document.getElementById('inputDescricao');
    if(n) document.getElementById('hiddenNome').value = n.value;
    if(d) document.getElementById('hiddenDescricao').value = d.value;
}
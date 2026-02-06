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
    
    // Se houver select2 na página, inicializa
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
    
    // Inicializa o DataTables
    var table = $('#tabelaSelecao').DataTable({
        language: {
            url: '//cdn.datatables.net/plug-ins/1.13.7/i18n/pt-BR.json',
            searchPlaceholder: "Buscar..."
        },
        pageLength: 9,
        lengthChange: false,
        ordering: false, // Importante: mantemos desligado para facilitar a manipulação do DOM
        dom: "<'row mb-3'<'col-sm-12'f>><'row'<'col-sm-12'tr>><'row mt-3'<'col-sm-12'p>>"
    });

    // Ajuste de estilo da busca
    $('.dataTables_filter input').addClass('form-control ps-3').css('border-radius', '8px');

    // --- FUNÇÃO CENTRAL: ALTERAR TODOS ---
    // Esta função controla a lógica de dados, independente do visual
    function toggleTodos(marcar) {
        // Pega os dados de TODAS as linhas filtradas (visíveis ou não)
        var dados = table.rows({ search: 'applied' }).data();
        
        dados.each(function(value) {
            // value[0] é o HTML da primeira coluna: <input ... value="123">
            // Usamos REGEX para extrair o valor, ignorando parsing de HTML que pode falhar
            var match = value[0].match(/value=["']([^"']+)["']/);
            var id = match ? match[1] : null;

            if (id) {
                if (marcar) {
                    idsSelecionados.add(id);
                } else {
                    idsSelecionados.delete(id);
                }
            }
        });

        atualizarVisual();
    }

    // --- FUNÇÃO CENTRAL: ATUALIZAR VISUAL ---
    function atualizarVisual() {
        // 1. Atualiza os checkboxes VISÍVEIS na página atual
        // Usamos table.nodes() para pegar apenas o que existe no DOM agora
        $(table.rows({ page: 'current' }).nodes()).each(function() {
            var input = $(this).find('.check-item');
            var id = input.val();
            
            if (idsSelecionados.has(id)) {
                input.prop('checked', true);
                $(this).addClass('bg-primary bg-opacity-10'); // Pinta a linha
            } else {
                input.prop('checked', false);
                $(this).removeClass('bg-primary bg-opacity-10');
            }
        });

        // 2. Atualiza contador e botão
        var total = idsSelecionados.size;
        var textoContador = total === 1 ? '1 contato selecionado' : total + ' contatos selecionados';
        
        $('#contadorSelecao').text(textoContador);
        if(total > 0) $('#contadorSelecao').removeClass('text-muted').addClass('text-primary');
        else $('#contadorSelecao').addClass('text-muted').removeClass('text-primary');

        // 3. Atualiza estado do Checkbox Mestre e Botão Grande
        // Lógica: Se todos da página ATUAL estão marcados, o mestre fica marcado
        var inputsPagina = $('.check-item', table.rows({ page: 'current' }).nodes());
        var totalPagina = inputsPagina.length;
        var marcadosPagina = inputsPagina.filter(':checked').length;
        
        var paginaCheia = (totalPagina > 0 && totalPagina === marcadosPagina);
        
        // Atualiza checkbox mestre sem disparar eventos
        $('#checkAll').prop('checked', paginaCheia);

        // Atualiza texto do botão grande
        if (paginaCheia || (total > 0 && total === table.rows({ search: 'applied' }).count())) {
            $('#txtBtnAll').text('Desmarcar Todos');
            $('#btnToggleAll').removeClass('btn-outline-primary').addClass('btn-primary text-white');
        } else {
            $('#txtBtnAll').text('Selecionar Todos da Lista');
            $('#btnToggleAll').addClass('btn-outline-primary').removeClass('btn-primary text-white');
        }
    }


    // --- EVENTOS ---

    // 1. Clique no Checkbox Mestre (#checkAll)
    $('#tabelaSelecao').on('click', '#checkAll', function() {
        var marcar = this.checked;
        toggleTodos(marcar);
    });

    // 2. Clique no Botão Grande (#btnToggleAll)
    $('#btnToggleAll').on('click', function(e) {
        e.preventDefault();
        
        // Verifica o estado atual do checkbox mestre para decidir se marca ou desmarca tudo
        var estaMarcado = $('#checkAll').prop('checked');
        var novaAcao = !estaMarcado; // Se estava marcado, vamos desmarcar
        
        toggleTodos(novaAcao);
    });

    // 3. Clique em Checkbox Individual (.check-item)
    $('#tabelaSelecao tbody').on('click', '.check-item', function(e) {
        e.stopPropagation(); // Evita conflito com o clique da linha
        var id = $(this).val();
        if (this.checked) idsSelecionados.add(id);
        else idsSelecionados.delete(id);
        atualizarVisual();
    });

    // 4. Clique na Linha (Melhoria de UX)
    $('#tabelaSelecao tbody').on('click', 'tr', function(e) {
        // Se clicar na linha (mas não no input), simulamos o clique no input
        if (e.target.type !== 'checkbox') {
            var checkbox = $(this).find('.check-item');
            checkbox.trigger('click');
        }
    });

    // 5. Redesenho da Tabela (Paginação, Filtro, Ordenação)
    table.on('draw', function() {
        atualizarVisual();
    });

    // --- CARREGAMENTO INICIAL ---
    // Recupera checkboxes já marcados (ex: reload após erro de validação)
    $('.check-item').each(function() {
        if(this.checked) idsSelecionados.add($(this).val());
    });
    atualizarVisual();

    // ==========================================================================
    // 3. SUBMISSÃO DO FORMULÁRIO
    // ==========================================================================
    $('#formGrupo').on('submit', function(e) {
        var form = this;
        
        // Limpa inputs hidden antigos para não duplicar
        $(form).find('input[name="idsContatos"]').remove();

        if (idsSelecionados.size === 0) {
            e.preventDefault();
            alert('Por favor, selecione pelo menos um contato.');
            return false;
        }

        // Cria um input hidden para cada ID selecionado
        idsSelecionados.forEach(function(id) {
            $(form).append(
                $('<input>')
                    .attr('type', 'hidden')
                    .attr('name', 'idsContatos')
                    .attr('value', id)
            );
        });
    });

});

// Função global para manter filtros na URL
function prepararFiltro() {
    var n = document.getElementById('inputNome');
    var d = document.getElementById('inputDescricao');
    if(n) document.getElementById('hiddenNome').value = n.value;
    if(d) document.getElementById('hiddenDescricao').value = d.value;
}
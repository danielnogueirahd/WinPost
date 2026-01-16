$(document).ready(function() {


    // Inicialização do DataTables
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



    // Função de atualização visual
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



    // Eventos
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
});



// FUNÇÃO GLOBAL DO SIDEBAR
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


// Agora sim, definimos a prepararFiltro no escopo global
function prepararFiltro() {
    // Pega o valor digitado no formulário de cadastro
    var nomeDigitado = document.getElementById('inputNome').value;
    var descDigitada = document.getElementById('inputDescricao').value;

    // Joga para os campos hidden do formulário de filtro
    document.getElementById('hiddenNome').value = nomeDigitado;
    document.getElementById('hiddenDescricao').value = descDigitada;

};
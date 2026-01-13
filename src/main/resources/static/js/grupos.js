$(document).ready(function() {
    
    // Inicializa o DataTable com as configurações
    // Se você já tiver essa inicialização no HTML, pode remover de lá e deixar só aqui
    var table = $('#tabela').DataTable({
        language: {
            url: 'https://cdn.datatables.net/plug-ins/1.13.7/i18n/pt-BR.json'
        },
        columnDefs: [
            { orderable: false, targets: 0 },  // Desabilita ordenação na coluna dos Checkboxes (primeira)
            { orderable: false, targets: -1 }  // Desabilita ordenação na coluna de Ações (última)
        ]
    });

    // 1. Lógica do "Selecionar Todos"
    $('#checkAll').on('click', function() {
        // Pega se está marcado ou não
        var isChecked = $(this).is(':checked');
        
        // Marca/Desmarca todos os checkboxes com a classe .check-contato
        $('.check-contato').prop('checked', isChecked);
        
        atualizarBotaoGrupo();
    });

    // 2. Lógica ao clicar em um checkbox individual
    // Usamos 'on' no document para funcionar mesmo se mudar de página na tabela
    $(document).on('change', '.check-contato', function() {
        atualizarBotaoGrupo();
        
        // Se desmarcar um item, desmarca o "Todos" lá em cima
        if(!$(this).is(':checked')){
            $('#checkAll').prop('checked', false);
        }
    });
});

// ==========================================
// FUNÇÕES AUXILIARES
// ==========================================

// Habilita ou Desabilita o botão de ação
function atualizarBotaoGrupo() {
    var selecionados = $('.check-contato:checked').length;
    var btn = $('#btnCriarGrupo');
    
    if (selecionados > 0) {
        btn.removeAttr('disabled');
        btn.removeClass('btn-light text-muted').addClass('btn-warning text-dark');
        btn.html('<i class="fa-solid fa-users-viewfinder"></i> Criar Grupo (' + selecionados + ')');
    } else {
        btn.attr('disabled', 'disabled');
        btn.addClass('btn-light text-muted').removeClass('btn-warning text-dark');
        btn.html('<i class="fa-solid fa-users-viewfinder"></i> Criar Grupo');
    }
}

// Abre o Modal e atualiza o contador
function abrirModalGrupo() {
    var qtd = $('.check-contato:checked').length;
    $('#qtdSelecionados').text(qtd);
    
    // Abre o modal usando Bootstrap 5
    var modal = new bootstrap.Modal(document.getElementById('modalCriarGrupo'));
    modal.show();
}

// Envia os dados para o Backend (Java)
function salvarGrupo() {
    var nome = $('#nomeGrupo').val();
    
    if (!nome) {
        // Você pode usar SweetAlert aqui se preferir
        alert("Por favor, digite um nome para o grupo.");
        return;
    }

    // Coleta os IDs dos checkboxes marcados
    var ids = [];
    $('.check-contato:checked').each(function() {
        ids.push($(this).val());
    });

    // Faz a chamada AJAX para o Controller
    $.ajax({
        url: "/grupos/criar",
        type: "POST",
        data: {
            nome: nome,
            ids: ids
        },
        success: function(response) {
            // Fecha o modal
            var modalEl = document.getElementById('modalCriarGrupo');
            var modal = bootstrap.Modal.getInstance(modalEl);
            modal.hide();
            
            // Mostra mensagem de sucesso
            Swal.fire({
                title: 'Sucesso!',
                text: response,
                icon: 'success',
                confirmButtonColor: '#0d6efd'
            }).then(() => {
                location.reload(); // Recarrega a página
            });
        },
        error: function(xhr) {
            Swal.fire('Erro', 'Ocorreu um erro ao criar o grupo.', 'error');
            console.error(xhr.responseText);
        }
    });
}
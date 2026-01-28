// Variável global para armazenar o ID selecionado para exclusão
let idSelecionado = null;

$(document).ready(function() {
    // Configura os botões de confirmação dos modais (Lixeira e Exclusão)
    // Usamos .off('click').on('click') para evitar múltiplos eventos se o modal for aberto várias vezes

    $('#btnConfirmarLixeira').on('click', function() {
        if(idSelecionado) {
            $.post('/mensagens/lixeira/' + idSelecionado, function() {
                location.reload(); // Recarrega a página após mover para lixeira
            });
        }
    });

    $('#btnConfirmarExclusaoDefinitiva').on('click', function() {
        if(idSelecionado) {
            $.ajax({
                url: '/mensagens/excluir/' + idSelecionado,
                type: 'DELETE',
                success: function() {
                    location.reload(); // Recarrega após excluir
                },
                error: function() {
                    alert('Erro ao excluir mensagem.');
                }
            });
        }
    });
});

// --- FUNÇÕES CHAMADAS PELO HTML (ONCLICK) ---

// 1. Ação do Botão "Usar Modelo"
function usarModelo(btn) {
    let id = $(btn).data('id');
    // Redireciona para a tela de preparação de envio
    window.location.href = '/mensagens/preparar-envio/' + id;
}

// 2. Abrir Modal de Criar Novo Modelo
function abrirModalCriacao() {
    var modalElement = document.getElementById('modalNovoModelo');
    if (modalElement) {
        var modal = new bootstrap.Modal(modalElement);
        modal.show();
    } else {
        console.error("Modal 'modalNovoModelo' não encontrado no HTML.");
    }
}

// 3. Abrir Modal de Lixeira
function abrirModalLixeira(id) {
    idSelecionado = id;
    var modal = new bootstrap.Modal(document.getElementById('modalLixeira'));
    modal.show();
}

// 4. Restaurar da Lixeira
function restaurarMensagem(id) {
    $.post('/mensagens/restaurar/' + id, function() {
        location.reload();
    });
}

// 5. Abrir Modal de Exclusão Definitiva
function abrirModalExclusaoDefinitiva(id) {
    idSelecionado = id;
    var modal = new bootstrap.Modal(document.getElementById('modalExclusaoDefinitiva'));
    modal.show();
}

// 6. Favoritar ou Marcar como Importante
function toggleAction(id, action, element, type) {
    $.post('/mensagens/' + action + '/' + id, function(state) {
        // Atualiza o ícone visualmente sem recarregar a página
        if(type === 'star') {
            $(element).toggleClass('fa-solid star-active fa-regular');
        }
        if(type === 'flag') {
            $(element).toggleClass('fa-solid flag-active fa-regular');
        }
    });
}

// 7. Visualizar Detalhes (Leitura)
function verMensagem(id) {
    $.get("/mensagens/detalhes/" + id, function(data) {
        // Preenche os campos do modal
        $('#modalAssunto').text(data.assunto);
        $('#modalConteudo').html(data.conteudo); // Renderiza HTML
        $('#modalGrupo').text(data.nomeGrupoDestino);
        
        // Avatar simples (Primeira letra do grupo ou 'W')
        let avatarLetra = 'W';
        if (data.nomeGrupoDestino && data.nomeGrupoDestino.length > 0) {
            avatarLetra = data.nomeGrupoDestino.charAt(0).toUpperCase();
        }
        $('#modalAvatar').text(avatarLetra);
        
        // Formatação de data
        if (data.dataEnvio) {
            const dt = new Date(data.dataEnvio);
            $('#modalData').text(dt.toLocaleDateString() + ' ' + dt.toLocaleTimeString());
        }
        
        // Exibe o modal
        var modal = new bootstrap.Modal(document.getElementById('modalLeitura'));
        modal.show();
    }).fail(function() {
        alert("Erro ao carregar detalhes da mensagem.");
    });
}
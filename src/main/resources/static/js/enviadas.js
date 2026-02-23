// Variável global para armazenar o ID selecionado para exclusão
let idSelecionado = null;

$(document).ready(function() {
    // 1. Configura botão de confirmar envio para LIXEIRA (Funciona para listagem e modal)
    $('#btnConfirmarLixeira').off('click').on('click', function() {
        if(idSelecionado) {
            $.post('/mensagens/lixeira/' + idSelecionado, function() {
                location.reload(); 
            });
        }
    });

    // 2. Configura botão de EXCLUSÃO DEFINITIVA
    $('#btnConfirmarExclusaoDefinitiva').off('click').on('click', function() {
        if(idSelecionado) {
            $.ajax({
                url: '/mensagens/excluir/' + idSelecionado,
                type: 'DELETE',
                success: function() {
                    location.reload(); 
                },
                error: function() {
                    alert('Erro ao excluir mensagem.');
                }
            });
        }
    });
});

// --- FUNÇÕES CHAMADAS PELO HTML ---

// 1. Visualizar Detalhes (Lógica Principal do Modal)
function verMensagem(id) {
    // Feedback visual de carregamento
    $('#modalConteudo').html('<div class="text-center py-5 text-muted"><i class="fa-solid fa-circle-notch fa-spin me-2"></i>Carregando...</div>');
    $('#modalAssunto').text('Carregando...');
    
    $.get("/mensagens/detalhes/" + id, function(data) {
        // A. Preenche os textos
        $('#modalAssunto').text(data.assunto);
        $('#modalConteudo').html(data.conteudo); 
        $('#modalGrupo').text(data.nomeGrupoDestino || 'Sem Grupo Definido');
        
        // B. Define o Avatar (Primeira letra)
        let avatarLetra = 'W';
        if (data.nomeGrupoDestino && data.nomeGrupoDestino.trim().length > 0) {
            avatarLetra = data.nomeGrupoDestino.trim().charAt(0).toUpperCase();
        }
        $('#modalAvatar').text(avatarLetra);
        
        // C. Formata a Data
        if (data.dataEnvio) {
            const dt = new Date(data.dataEnvio);
            const opcoes = { day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' };
            $('#modalData').text(dt.toLocaleDateString('pt-BR', opcoes));
        } else {
            $('#modalData').text('Data desconhecida');
        }

        // D. Configura o botão de LIXEIRA do cabeçalho do modal
        // Remove eventos anteriores (.off) e adiciona o novo (.on)
        $('#btnExcluirModal').off('click').on('click', function() {
            // 1. Esconde o modal de leitura
            var modalLeitura = bootstrap.Modal.getInstance(document.getElementById('modalLeitura'));
            modalLeitura.hide();
            
            // 2. Abre o modal de confirmação de lixeira com o ID certo
            abrirModalLixeira(id);
        });

        // E. Abre o modal
        var modal = new bootstrap.Modal(document.getElementById('modalLeitura'));
        modal.show();

    }).fail(function() {
        $('#modalConteudo').html('<div class="text-center text-danger py-4">Erro ao carregar mensagem.</div>');
    });
}

// 2. Função de Imprimir (Gera janela limpa)
function imprimirMensagem() {
    const conteudo = document.getElementById('modalConteudo').innerHTML;
    const assunto = document.getElementById('modalAssunto').innerText;
    const data = document.getElementById('modalData').innerText;
    const grupo = document.getElementById('modalGrupo').innerText;

    // Abre janela vazia
    const janela = window.open('', '', 'height=600,width=800');
    
    janela.document.write('<html><head><title>Impressão - WinPost</title>');
    janela.document.write(`
        <style>
            body { font-family: 'Segoe UI', Arial, sans-serif; padding: 40px; color: #333; }
            .header { border-bottom: 2px solid #eee; padding-bottom: 20px; margin-bottom: 30px; }
            h1 { font-size: 24px; margin: 0 0 10px 0; color: #000; }
            .meta { font-size: 14px; color: #666; margin-bottom: 5px; }
            .content { line-height: 1.6; font-size: 16px; }
        </style>
    `);
    janela.document.write('</head><body>');
    
    janela.document.write('<div class="header">');
    janela.document.write('<h1>' + assunto + '</h1>');
    janela.document.write('<div class="meta"><strong>Para:</strong> ' + grupo + '</div>');
    janela.document.write('<div class="meta"><strong>Enviado em:</strong> ' + data + '</div>');
    janela.document.write('</div>');
    
    janela.document.write('<div class="content">' + conteudo + '</div>');
    
    janela.document.write('</body></html>');
    
    janela.document.close();
    janela.focus();
    
    // Pequeno delay para garantir que o CSS carregou antes de abrir a caixa de impressão
    setTimeout(function() { 
        janela.print(); 
        // Opcional: janela.close(); // Se quiser fechar a janela automaticamente após imprimir
    }, 500); 
}

// 3. Abrir Modal de Lixeira (Chamado pela listagem ou pelo modal de leitura)
function abrirModalLixeira(id) {
    idSelecionado = id;
    var modal = new bootstrap.Modal(document.getElementById('modalLixeira'));
    modal.show();
}

// 4. Outras Funções Utilitárias
function usarModelo(btn) {
    let id = $(btn).data('id');
    window.location.href = '/mensagens/preparar-envio/' + id;
}

function abrirModalCriacao() {
    var modalElement = document.getElementById('modalNovoModelo');
    if (modalElement) {
        var modal = new bootstrap.Modal(modalElement);
        modal.show();
    }
}

function restaurarMensagem(id) {
    $.post('/mensagens/restaurar/' + id, function() {
        location.reload();
    });
}

function abrirModalExclusaoDefinitiva(id) {
    idSelecionado = id;
    var modal = new bootstrap.Modal(document.getElementById('modalExclusaoDefinitiva'));
    modal.show();
}

function toggleAction(id, action, element, type) {
    // Envia a requisição para o backend
    $.post('/mensagens/' + action + '/' + id, function(state) {
        if(type === 'star') {
            // Alterna tanto o formato (solid/regular) quanto a cor (warning/muted)
            $(element).toggleClass('fa-solid text-warning fa-regular text-muted');
        }
        if(type === 'flag') {
            // Alterna o formato e a cor da bandeira (info/muted)
            $(element).toggleClass('fa-solid text-info fa-regular text-muted');
        }
    }).fail(function() {
        console.error("Erro ao atualizar o status da mensagem.");
    });
}
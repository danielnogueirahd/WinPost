// Variável global para armazenar o ID da mensagem selecionada/aberta
let idSelecionado = null;

$(document).ready(function() {
    
    // 1. Inicia Tooltips do Bootstrap
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
      return new bootstrap.Tooltip(tooltipTriggerEl)
    });

    // 2. Configura botões de Ação (Lixeira / Exclusão Definitiva)
    $('#btnConfirmarLixeira').off('click').on('click', function() {
        if(idSelecionado) {
            $.post('/mensagens/lixeira/' + idSelecionado, function() {
                location.reload(); 
            });
        }
    });

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

// --- FUNÇÕES PRINCIPAIS ---

// 1. Visualizar Detalhes (Busca dados no Java e abre o Modal)
function verMensagem(id, focarNota = false) {
    idSelecionado = id;

    // Feedback visual inicial
    $('#modalConteudo').html('<div class="text-center py-5 text-muted"><i class="fa-solid fa-circle-notch fa-spin me-2"></i>Carregando conteúdo...</div>');
    $('#modalAssunto').text('Carregando...');
    
    // Abre o modal
    var modalEl = document.getElementById('modalLeitura');
    var modal = new bootstrap.Modal(modalEl);
    modal.show();

    // Faz a requisição ao Back-end
    $.get("/mensagens/detalhes/" + id, function(data) {
        
        // A. Preenche os textos principais
        $('#modalAssunto').text(data.assunto);
        $('#modalConteudo').html(data.conteudo); 
        $('#modalGrupo').text(data.nomeGrupoDestino || 'Sem Grupo');
        
        // B. Preenche a Observação (Vinda do Banco de Dados)
        // Se for null, coloca string vazia
        $('#modalObservacao').val(data.observacao || '');

        // C. Define o Avatar (Primeira letra)
        let letra = 'W';
        if (data.nomeGrupoDestino && data.nomeGrupoDestino.trim().length > 0) {
            letra = data.nomeGrupoDestino.trim().charAt(0).toUpperCase();
        }
        $('#modalAvatar').text(letra);
        
        // D. Formata a Data
        if (data.dataEnvio) {
            const dt = new Date(data.dataEnvio);
            const opcoes = { day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' };
            $('#modalData').text(dt.toLocaleDateString('pt-BR', opcoes));
        } else {
            $('#modalData').text('');
        }

        // E. Configura botão de excluir do modal
        $('#btnExcluirModal').off('click').on('click', function() {
            modal.hide();
            abrirModalLixeira(id);
        });

        // F. Foca na nota se foi solicitado (clique no ícone da lista)
        if(focarNota) {
            setTimeout(() => {
                const campoNota = document.getElementById('modalObservacao');
                if(campoNota) {
                    campoNota.focus();
                    // Pequeno flash visual
                    campoNota.style.transition = "background-color 0.3s";
                    campoNota.style.backgroundColor = "#fff"; 
                    setTimeout(() => { campoNota.style.backgroundColor = ""; }, 600);
                }
            }, 500); 
        }

    }).fail(function() {
        $('#modalConteudo').html('<div class="text-center text-danger py-4">Erro ao carregar mensagem.</div>');
    });
}

// 2. Atalho: Abrir direto na Nota (clicado na lista)
function abrirNota(id, event) {
    if (event) event.stopPropagation();
    // Chama a função principal com flag true
    verMensagem(id, true);
}

// 3. Salvar Nota (Ajax Real)
function salvarNota(btnElement) {
    const originalContent = btnElement.innerHTML;
    const textoNota = $('#modalObservacao').val();
    
    // Feedback "Salvando..."
    btnElement.innerHTML = '<i class="fa-solid fa-circle-notch fa-spin"></i> Salvando...';
    btnElement.classList.add('opacity-75');
    
    // Envia para o Back-end
    $.post('/mensagens/salvarObservacao', { id: idSelecionado, observacao: textoNota })
        .done(function() {
            // Sucesso Visual
            btnElement.innerHTML = '<i class="fa-solid fa-check-double"></i> Salvo!';
            btnElement.classList.remove('btn-warning', 'text-dark', 'opacity-75');
            btnElement.classList.add('btn-success', 'text-white', 'btn-salvo'); 
            
            // Volta ao estado original após 1.5s
            setTimeout(() => {
                btnElement.innerHTML = originalContent;
                btnElement.classList.remove('btn-success', 'text-white', 'btn-salvo');
                btnElement.classList.add('btn-warning', 'text-dark');
                
                // Opcional: Se quiser atualizar o ícone da lista sem recarregar tudo, precisaria de mais lógica JS.
                // Por enquanto, o ícone só atualiza se der F5.
            }, 1500);
        })
        .fail(function() {
            alert("Erro ao salvar a anotação.");
            btnElement.innerHTML = originalContent;
            btnElement.classList.remove('opacity-75');
        });
}


// --- OUTRAS FUNÇÕES AUXILIARES ---

function imprimirMensagem() {
    const conteudo = document.getElementById('modalConteudo').innerHTML;
    const assunto = document.getElementById('modalAssunto').innerText;
    const data = document.getElementById('modalData').innerText;
    const grupo = document.getElementById('modalGrupo').innerText;

    const janela = window.open('', '', 'height=600,width=800');
    
    janela.document.write('<html><head><title>Impressão - WinPost</title>');
    janela.document.write(`
        <style>
            body { font-family: 'Segoe UI', Arial, sans-serif; padding: 40px; color: #333; }
            .header { border-bottom: 2px solid #eee; padding-bottom: 20px; margin-bottom: 30px; }
            h1 { font-size: 24px; margin: 0 0 10px 0; color: #000; }
            .meta { font-size: 14px; color: #666; margin-bottom: 5px; }
            .content { line-height: 1.6; font-size: 16px; }
            img { max-width: 100%; }
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
    
    setTimeout(function() { 
        janela.print(); 
    }, 500); 
}

function abrirModalLixeira(id) {
    idSelecionado = id;
    var modal = new bootstrap.Modal(document.getElementById('modalLixeira'));
    modal.show();
}

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
    $.post('/mensagens/' + action + '/' + id, function(state) {
        if(type === 'star') {
            $(element).toggleClass('fa-solid text-warning fa-regular text-muted');
        }
    }).fail(function() {
        console.error("Erro ao atualizar status.");
    });
}
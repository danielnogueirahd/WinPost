// --- agenda.js ---

/**
 * 1. Abre o modal de cadastro de NOVO evento
 * Limpa o formulário e define a data selecionada
 */
function abrirModalNovoEvento(dia, mes, ano) {
    // Se o modal de detalhes estiver aberto, fecha ele antes
    var elDetalhes = document.getElementById('modalDetalhesDia');
    if (elDetalhes) {
        var modalDetalhes = bootstrap.Modal.getInstance(elDetalhes);
        if (modalDetalhes) modalDetalhes.hide();
    }

    // Formata data para o input datetime-local (YYYY-MM-DDTHH:mm)
    var mesFmt = mes < 10 ? '0' + mes : mes;
    var diaFmt = dia < 10 ? '0' + dia : dia;
    var dataHoraISO = `${ano}-${mesFmt}-${diaFmt}T09:00`;

    // Reseta os campos
    $('#modalNovoEvento input[name="dataHora"]').val(dataHoraISO);
    $('#modalNovoEvento input[name="titulo"]').val('');
    $('#modalNovoEvento textarea[name="descricao"]').val('');
    $('#modalNovoEvento select[name="contato"]').val(''); 
    
    // Reseta a seleção de tipos para o padrão
    alternarModoCriacao(false);
    $('#tipoTarefa').prop('checked', true);

    // Abre o modal
    var modalNovo = new bootstrap.Modal(document.getElementById('modalNovoEvento'));
    modalNovo.show();
}

/**
 * 2. Abre o modal de DETALHES do dia (Listagem)
 * Busca os eventos via AJAX e monta a lista com botões de ação
 */
function verDetalhesDia(dia, mes, ano) {
    var dataVisual = dia + '/' + mes + '/' + ano;
    $('#modalTituloData').text(dataVisual);
    
    var mesFormatado = mes < 10 ? '0' + mes : mes;
    var diaFormatado = dia < 10 ? '0' + dia : dia;
    var dataISO = ano + '-' + mesFormatado + '-' + diaFormatado;

    // Configura o botão "Adicionar" deste modal para abrir o formulário na data certa
    $('#btnAdicionarNestaData').attr('onclick', `abrirModalNovoEvento(${dia}, ${mes}, ${ano})`);
    
    // Loading inicial
    $('#listaDetalhes').html('<div class="text-center py-4"><div class="spinner-border text-primary spinner-border-sm"></div><p class="mt-2 small text-muted">Carregando...</p></div>');
    $('#msgVazio').addClass('d-none');
    
    var modal = new bootstrap.Modal(document.getElementById('modalDetalhesDia'));
    modal.show();

    // Chamada AJAX para buscar os detalhes
    $.get('/administrativo/agenda/detalhes?data=' + dataISO, function(dados) {
        var html = '';
        
        if (!dados || dados.length === 0) {
            $('#listaDetalhes').empty();
            $('#msgVazio').removeClass('d-none');
        } else {
            $('#msgVazio').addClass('d-none');
            
            dados.forEach(function(item) {
                var icone = 'fa-circle';
                var corIcone = 'text-secondary';
                var bgIcone = 'bg-light';
                var botoesAcao = '';

                // --- Configuração Visual baseada no Tipo ---
                if (item.tipo === 'NIVER') { 
                    icone = 'fa-cake-candles'; corIcone = 'text-success'; bgIcone = 'bg-success bg-opacity-10';
                    // Nivers não têm botão de excluir aqui
                } 
                else if (item.tipo === 'ENVIO') { 
                    icone = 'fa-paper-plane'; corIcone = 'text-primary'; bgIcone = 'bg-primary bg-opacity-10';
                    // Botão para VER a mensagem enviada
                    botoesAcao = `<button onclick="verMensagem(${item.idRef})" class="btn btn-sm btn-outline-primary rounded-circle" title="Ler Mensagem"><i class="fa-solid fa-eye"></i></button>`;
                } 
                else if (item.tipo === 'FERIADO') { 
                    icone = 'fa-calendar-check'; corIcone = 'text-danger'; bgIcone = 'bg-danger bg-opacity-10';
                    // Feriados são fixos, sem botão de excluir
                }
                else {
                    // --- TAREFAS, REUNIÕES E PERSONALIZADOS (Podem ser excluídos) ---
                    if (item.tipo === 'REUNIAO') { icone = 'fa-users'; corIcone = 'text-info'; bgIcone = 'bg-info bg-opacity-10'; }
                    else if (item.tipo === 'IMPORTANTE') { icone = 'fa-triangle-exclamation'; corIcone = 'text-danger'; bgIcone = 'bg-danger bg-opacity-10'; }
                    else if (item.tipo === 'TAREFA') { icone = 'fa-list-check'; corIcone = 'text-secondary'; bgIcone = 'bg-secondary bg-opacity-10'; }
                    else { icone = 'fa-tag'; corIcone = 'text-dark'; bgIcone = 'bg-warning bg-opacity-10'; }

                    // Botão de Excluir (X) com SweetAlert
                    botoesAcao = `
                        <button class="btn btn-outline-danger btn-sm rounded-circle shadow-sm" 
                                onclick="confirmarExclusao(${item.idRef})" 
                                title="Cancelar/Excluir"
                                style="width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;">
                            <i class="fas fa-times"></i>
                        </button>
                    `;
                }

                // Renderiza o item da lista
                html += `
                    <div class="list-group-item d-flex justify-content-between align-items-center py-3 border-bottom">
                        <div class="d-flex align-items-center">
                            <div class="rounded-circle p-2 me-3 d-flex align-items-center justify-content-center ${bgIcone} ${corIcone}" style="width: 40px; height: 40px;">
                                <i class="fa-solid ${icone} fs-5"></i>
                            </div>
                            <div>
                                <h6 class="mb-0 fw-bold text-dark">${item.titulo}</h6>
                                <small class="text-muted" style="font-size: 0.8rem;">
                                    <span class="badge bg-light text-secondary border me-1">${item.tipo}</span>
                                    ${item.subtitulo || ''}
                                </small>
                            </div>
                        </div>
                        <div class="ms-2">
                            ${botoesAcao}
                        </div>
                    </div>
                `;
            });
            $('#listaDetalhes').html(html);
        }
    });
}

/**
 * 3. Confirmação e Exclusão (SweetAlert2)
 */
function confirmarExclusao(id) {
    Swal.fire({
        title: 'Tem certeza?',
        text: "Você deseja cancelar este agendamento? Essa ação não pode ser desfeita.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Sim, excluir',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            excluirEvento(id);
        }
    });
}

function excluirEvento(id) {
    // Verifica se o ID é válido antes de enviar
    if (!id) {
        Swal.fire('Erro!', 'ID do evento inválido ou indefinido.', 'error');
        return;
    }

    $.ajax({
        url: '/administrativo/agenda/remover/' + id,
        type: 'GET', // <--- MUDANÇA IMPORTANTE: Trocado de DELETE para GET para evitar bloqueios
        success: function(response) {
            Swal.fire({
                title: 'Excluído!',
                text: 'O agendamento foi removido com sucesso.',
                icon: 'success',
                timer: 1500,
                showConfirmButton: false
            }).then(() => {
                location.reload(); 
            });
        },
        error: function(xhr) {
            // Diagnóstico de erro
            let msgErro = "Erro desconhecido.";
            
            if (xhr.responseText) {
                msgErro = xhr.responseText;
            } else if (xhr.status === 403) {
                msgErro = "Acesso negado (403). Verifique se está logado.";
            } else if (xhr.status === 404) {
                msgErro = "Evento não encontrado no banco de dados (404).";
            } else if (xhr.status === 405) {
                msgErro = "Método não permitido (405). Tente reiniciar o servidor.";
            }

            console.error("Erro ao excluir:", xhr);
            
            Swal.fire(
                'Ops! Deu erro.',
                'O servidor respondeu: ' + msgErro,
                'error'
            );
        }
    });
}

/**
 * 4. Ver detalhes de uma mensagem (ENVIO)
 */
function verMensagem(id) {
    $('#modalConteudo').html('<div class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></div>');
    var modal = new bootstrap.Modal(document.getElementById('modalLeitura'));
    modal.show();
    
    $.get("/mensagens/detalhes/" + id, function(data) {
        $('#modalAssunto').text(data.assunto);
        $('#modalGrupo').text(data.nomeGrupoDestino || 'Destinatário Único');
        $('#modalData').text(new Date(data.dataEnvio).toLocaleString('pt-BR'));
        
        if(data.nomesAnexos) {
            $('#modalAnexos').text(data.nomesAnexos);
            $('#areaAnexosModal').removeClass('d-none');
        } else {
            $('#areaAnexosModal').addClass('d-none');
        }
        
        $('#modalConteudo').html(data.conteudo);
        $('#modalConteudo img').addClass('img-fluid rounded shadow-sm'); 
    });
}

/**
 * 5. Lógica Visual do Modal Novo Evento (Criar Tipo)
 */
function alternarModoCriacao(mostrar) {
    if (mostrar) {
        $('#areaSelecaoTipos').addClass('d-none');
        $('#btnNovoTipo').addClass('d-none');
        $('#areaCriacaoTipo').removeClass('d-none').addClass('fade-in');
    } else {
        $('#areaCriacaoTipo').addClass('d-none');
        $('#areaSelecaoTipos').removeClass('d-none').addClass('fade-in');
        $('#btnNovoTipo').removeClass('d-none');
    }
}

function salvarNovoTipo() {
    const nome = $('#novoTipoNome').val();
    const icone = $('#novoTipoIcone').val();
    const cor = $('#novoTipoCor').val();

    if (!nome) {
        if(typeof Swal !== 'undefined') Swal.fire('Atenção', 'Dê um nome ao tipo.', 'warning');
        else alert("Por favor, dê um nome ao tipo.");
        return;
    }

    const payload = {
        nome: nome,
        icone: icone,
        corHex: cor
    };

    const btn = document.querySelector('#areaCriacaoTipo button'); 
    const textoOriginal = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Salvando...';
    btn.disabled = true;

    $.ajax({
        url: '/api/tipos-evento', 
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(payload),
        success: function(novoTipo) {
            const novoId = 'tipo' + novoTipo.id;
            
            const htmlCard = `
                <input type="radio" class="btn-check" name="tipo" id="${novoId}" value="${novoTipo.nome}" checked>
                <label class="btn btn-outline-light text-start p-3 flex-fill border shadow-sm position-relative type-card fade-in" 
                       for="${novoId}" style="min-width: 140px; --cor-personalizada: ${novoTipo.corHex};">
                    <div class="d-flex flex-column align-items-center text-center">
                        <div class="icon-box bg-opacity-10 rounded-circle mb-2 d-flex align-items-center justify-content-center" 
                             style="width: 35px; height: 35px; background-color: ${novoTipo.corHex}20; color: ${novoTipo.corHex}">
                            <i class="fa-solid ${novoTipo.icone}"></i>
                        </div>
                        <div class="fw-bold text-dark small">${novoTipo.nome}</div>
                    </div>
                    <div class="check-indicator" style="color: ${novoTipo.corHex};"><i class="fa-solid fa-circle-check"></i></div>
                </label>
            `;
            
            $('#areaSelecaoTipos').append(htmlCard);
            alternarModoCriacao(false);
            $('#novoTipoNome').val('');
        },
        error: function() {
             if(typeof Swal !== 'undefined') Swal.fire('Erro', 'Erro ao criar tipo. Verifique o servidor.', 'error');
             else alert('Erro ao criar tipo.');
        },
        complete: function() {
            btn.innerHTML = textoOriginal;
            btn.disabled = false;
        }
    });
}

// Inicialização e CSS extra para animações
document.addEventListener('DOMContentLoaded', function() {
    if (!document.getElementById('styleFadeIn')) {
        document.head.insertAdjacentHTML("beforeend", `
            <style id="styleFadeIn">
                .fade-in { animation: fadeIn 0.3s ease-in-out; } 
                @keyframes fadeIn { from { opacity: 0; transform: translateY(-5px); } to { opacity: 1; transform: translateY(0); } }
            </style>`);
    }
});
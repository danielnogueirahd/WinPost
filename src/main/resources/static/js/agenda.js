// --- agenda.js (Versão Final - Modal Safe) ---

/**
 * Função Auxiliar: Fecha o modal de lista para evitar travamento
 */
function fecharModalLista() {
    var el = document.getElementById('modalDetalhesDia');
    if (el) {
        var modal = bootstrap.Modal.getInstance(el);
        if (modal) {
            modal.hide();
        }
    }
}

/**
 * 1. Abre o modal de cadastro de NOVO evento
 */
function abrirModalNovoEvento(dia, mes, ano) {
    fecharModalLista(); // Fecha a lista antes de abrir o novo

    setTimeout(() => {
        var mesFmt = mes < 10 ? '0' + mes : mes;
        var diaFmt = dia < 10 ? '0' + dia : dia;
        var dataHoraISO = `${ano}-${mesFmt}-${diaFmt}T09:00`;

        $('#inputId').val(''); 
        $('#modalNovoEvento input[name="dataHora"]').val(dataHoraISO);
        $('#modalNovoEvento input[name="titulo"]').val('');
        $('#modalNovoEvento textarea[name="descricao"]').val('');
        
        $('#tempData').val(`${ano}-${mesFmt}-${diaFmt}`);
        $('#tempHora').val('09:00');
        $('#inputDataHoraCompleta').val(dataHoraISO);

        alternarModoCriacao(false);
        $('#tipoTarefa').prop('checked', true);

        var modalNovo = new bootstrap.Modal(document.getElementById('modalNovoEvento'));
        modalNovo.show();
    }, 150);
}

/**
 * 2. Abre o modal de DETALHES do dia (Listagem)
 */
function verDetalhesDia(dia, mes, ano) {
    var dataVisual = dia + '/' + mes + '/' + ano;
    $('#modalTituloData').text(dataVisual);
    
    var mesFormatado = mes < 10 ? '0' + mes : mes;
    var diaFormatado = dia < 10 ? '0' + dia : dia;
    var dataISO = ano + '-' + mesFormatado + '-' + diaFormatado;

    $('#btnAdicionarNestaData').attr('onclick', `abrirModalNovoEvento(${dia}, ${mes}, ${ano})`);
    
    $('#listaDetalhes').html('<div class="text-center py-4"><div class="spinner-border text-primary spinner-border-sm"></div></div>');
    $('#msgVazio').addClass('d-none');
    
    var elModal = document.getElementById('modalDetalhesDia');
    var modalInstance = bootstrap.Modal.getInstance(elModal) || new bootstrap.Modal(elModal);
    modalInstance.show();

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

                // Verifica se o ID veio nulo (para debug)
                if(!item.idRef && item.tipo === 'ENVIO') {
                    console.error("ERRO: Item de ENVIO sem ID recebido!", item);
                }

                if (item.tipo === 'NIVER') { 
                    icone = 'fa-cake-candles'; corIcone = 'text-success'; bgIcone = 'bg-success bg-opacity-10';
                } 
                else if (item.tipo === 'ENVIO') { 
                    icone = 'fa-paper-plane'; corIcone = 'text-primary'; bgIcone = 'bg-primary bg-opacity-10';
                    // BOTÃO OLHO (Usa item.idRef)
                    botoesAcao = `<button onclick="verMensagem(${item.idRef})" class="btn btn-sm btn-outline-primary rounded-circle" title="Ler Mensagem"><i class="fa-solid fa-eye"></i></button>`;
                } 
                else if (item.tipo === 'FERIADO') { 
                    icone = 'fa-calendar-check'; corIcone = 'text-danger'; bgIcone = 'bg-danger bg-opacity-10';
                }
                else {
                    if (item.tipo === 'REUNIAO') { icone = 'fa-users'; corIcone = 'text-info'; bgIcone = 'bg-info bg-opacity-10'; }
                    else if (item.tipo === 'IMPORTANTE') { icone = 'fa-triangle-exclamation'; corIcone = 'text-danger'; bgIcone = 'bg-danger bg-opacity-10'; }
                    else if (item.tipo === 'TAREFA') { icone = 'fa-list-check'; corIcone = 'text-secondary'; bgIcone = 'bg-secondary bg-opacity-10'; }
                    else { icone = 'fa-tag'; corIcone = 'text-dark'; bgIcone = 'bg-warning bg-opacity-10'; }

                    botoesAcao = `
                        <div class="d-flex gap-2">
                            <button class="btn btn-outline-warning btn-sm rounded-circle" onclick="editarEvento(${item.idRef})" title="Editar"><i class="fa-solid fa-pen"></i></button>
                            <button class="btn btn-outline-danger btn-sm rounded-circle" onclick="confirmarExclusao(${item.idRef})" title="Excluir"><i class="fas fa-times"></i></button>
                        </div>
                    `;
                }

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
                        <div class="ms-2">${botoesAcao}</div>
                    </div>
                `;
            });
            $('#listaDetalhes').html(html);
        }
    });
}

/**
 * 3. Ver Mensagem (O "OLHO")
 */
function verMensagem(id) {
    if (!id) {
        alert("Erro: ID da mensagem não encontrado. Verifique o Java.");
        return;
    }

    fecharModalLista(); // Fecha a lista

    setTimeout(() => {
        var el = document.getElementById('modalLeitura');
        var modalLeitura = new bootstrap.Modal(el);
        modalLeitura.show();
        
        $('#modalConteudo').html('<div class="text-center py-4"><div class="spinner-border text-primary"></div></div>');
        $('#modalAssunto').text('Carregando...');
        
        $.get("/mensagens/detalhes/" + id, function(data) {
            $('#modalAssunto').text(data.assunto);
            $('#modalGrupo').text(data.nomeGrupoDestino || 'Destinatário Único');
            
            if(data.dataEnvio) {
                let dt = new Date(data.dataEnvio);
                $('#modalData').text(dt.toLocaleDateString('pt-BR') + ' ' + dt.toLocaleTimeString('pt-BR', {hour: '2-digit', minute:'2-digit'}));
            }

            if(data.nomesAnexos) {
                $('#modalAnexos').text(data.nomesAnexos);
                $('#areaAnexosModal').removeClass('d-none');
            } else {
                $('#areaAnexosModal').addClass('d-none');
            }
            
            $('#modalConteudo').html(data.conteudo);
            $('#modalConteudo img').addClass('img-fluid rounded shadow-sm'); 

        }).fail(function() {
            $('#modalConteudo').html('<p class="text-danger text-center">Erro ao carregar mensagem. Tente novamente.</p>');
        });
    }, 150);
}

/**
 * 4. Editar Evento (O "LÁPIS")
 */
function editarEvento(id) {
    if (!id) return;

    fecharModalLista(); // Fecha a lista

    setTimeout(() => {
        $.get('/administrativo/agenda/buscar/' + id, function(evento) {
            
            $('#inputId').val(evento.id);
            $('#inputTitulo').val(evento.titulo);
            $('#modalNovoEvento textarea[name="descricao"]').val(evento.descricao);

            if(evento.dataHora) {
                let partes = evento.dataHora.split('T');
                $('#tempData').val(partes[0]);
                $('#tempHora').val(partes[1].substring(0, 5));
                $('#inputDataHoraCompleta').val(evento.dataHora);
            }

            $('input[name="tipo"]').prop('checked', false);
            let radio = $(`input[name="tipo"][value="${evento.tipo}"]`);
            if(radio.length > 0) radio.prop('checked', true);
            else $('#tipoTarefa').prop('checked', true);
            
            alternarModoCriacao(false); 
            
            var modalNovo = new bootstrap.Modal(document.getElementById('modalNovoEvento'));
            modalNovo.show();
            
        }).fail(function() {
            alert("Erro ao buscar dados do evento.");
            // Reabre a lista se falhar
            var el = document.getElementById('modalDetalhesDia');
            if(el) new bootstrap.Modal(el).show();
        });
    }, 150);
}

/**
 * 5. Excluir Evento
 */
function confirmarExclusao(id) {
    fecharModalLista();

    Swal.fire({
        title: 'Tem certeza?',
        text: "Deseja remover este item da agenda?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Sim, excluir',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: '/administrativo/agenda/remover/' + id,
                type: 'GET',
                success: function() {
                    Swal.fire({ title: 'Excluído!', icon: 'success', timer: 1500, showConfirmButton: false })
                    .then(() => location.reload());
                },
                error: function() {
                    Swal.fire('Erro', 'Não foi possível excluir.', 'error');
                }
            });
        } else {
            var el = document.getElementById('modalDetalhesDia');
            if(el) new bootstrap.Modal(el).show();
        }
    });
}

// Funções de Criação de Tipo (mantidas)
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

    if (!nome) { Swal.fire('Atenção', 'Dê um nome ao tipo.', 'warning'); return; }

    const payload = { nome: nome, icone: icone, corHex: cor };
    const btn = document.querySelector('#areaCriacaoTipo button'); 
    const textoOriginal = btn.innerHTML;
    btn.innerHTML = 'Salvando...'; btn.disabled = true;

    $.ajax({
        url: '/api/tipos-evento', type: 'POST', contentType: 'application/json',
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
                </label>`;
            $('#areaSelecaoTipos').append(htmlCard);
            alternarModoCriacao(false);
            $('#novoTipoNome').val('');
        },
        error: function() { Swal.fire('Erro', 'Erro ao criar tipo.', 'error'); },
        complete: function() { btn.innerHTML = textoOriginal; btn.disabled = false; }
    });
}
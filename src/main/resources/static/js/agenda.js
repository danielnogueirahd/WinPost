// --- agenda.js (Versão Final Corrigida e Sincronizada) ---

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
                var badgeStatus = '';

                // 1. Limpeza do Título (Remove o "[CONCLUÍDA]" do texto e transforma em visual)
                var isConcluida = item.titulo.includes('[CONCLUÍDA]');
                var tituloExibicao = item.titulo.replace(' [CONCLUÍDA]', '').replace('[CONCLUÍDA]', '').trim();
                var classeTitulo = isConcluida ? 'text-muted text-decoration-line-through' : 'text-dark';
                var opacidadeIcone = isConcluida ? 'opacity-50' : '';

                // 2. Detecção Inteligente
                var t = item.titulo.toLowerCase();
                var isLembreteEnvio = t.includes('enviar') || t.includes('disparo') || t.includes('mensagem') || t.includes('campanha');

                var rotaDisparo = '/mensagens/enviadas';
                if (item.tipo === 'ENVIO' && item.idRef) { rotaDisparo = '/mensagens/preparar-envio/' + item.idRef; }
                var atalhoDisparo = `<a href="${rotaDisparo}" class="btn-acao disparar" title="Ir para Tela de Disparo"><i class="fa-solid fa-paper-plane"></i></a>`;

                // 3. Tags de Status (Atrasado, Hoje, Enviado, Concluída)
                if (item.tipo === 'ENVIO') {
                    badgeStatus = `<span class="badge bg-success text-white fw-normal"><i class="fa-solid fa-check-double"></i> Enviado</span>`;
                } else if (isLembreteEnvio) {
                    var dataEvento = new Date(ano, mes - 1, dia);
                    var hoje = new Date(); hoje.setHours(0, 0, 0, 0);

                    if (dataEvento < hoje && !isConcluida) {
                        badgeStatus = `<span class="badge bg-danger text-white fw-normal"><i class="fa-solid fa-xmark"></i> Atrasado</span>`;
                    } else if (dataEvento.getTime() === hoje.getTime() && !isConcluida) {
                        badgeStatus = `<span class="badge bg-warning text-dark fw-normal"><i class="fa-solid fa-clock"></i> Fazer Hoje</span>`;
                    }
                }

                if (isConcluida) {
                    badgeStatus += `<span class="badge bg-light text-success border border-success fw-bold ms-1"><i class="fa-solid fa-check"></i> Concluída</span>`;
                }

                // 4. Ícones e Cores
                if (item.tipo === 'NIVER') { icone = 'fa-cake-candles'; corIcone = 'text-white'; bgIcone = 'bg-success'; }
                else if (item.tipo === 'ENVIO') {
                    icone = 'fa-paper-plane'; corIcone = 'text-white'; bgIcone = 'bg-primary';
                    botoesAcao = `<div class="d-flex gap-1 align-items-center"><button onclick="verMensagem(${item.idRef})" class="btn-acao editar" title="Ler Mensagem"><i class="fa-solid fa-eye"></i></button>${atalhoDisparo}</div>`;
                }
                else if (item.tipo === 'FERIADO') { icone = 'fa-calendar-check'; corIcone = 'text-white'; bgIcone = 'bg-danger'; }
                else {
                    if (item.tipo === 'REUNIAO') { icone = 'fa-users'; corIcone = 'text-white'; bgIcone = 'bg-info'; }
                    else if (item.tipo === 'IMPORTANTE') { icone = 'fa-triangle-exclamation'; corIcone = 'text-white'; bgIcone = 'bg-danger'; }
                    else if (item.tipo === 'TAREFA') { icone = 'fa-list-check'; corIcone = 'text-white'; bgIcone = 'bg-secondary'; }
                    else { icone = 'fa-tag'; corIcone = 'text-dark'; bgIcone = 'bg-warning'; }

                    botoesAcao = `
                        <div class="d-flex gap-1 align-items-center">
                            ${isLembreteEnvio && !isConcluida ? atalhoDisparo : ''}
                            <button class="btn-acao editar" onclick="editarEvento(${item.idRef})" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>
                            <button class="btn-acao excluir" onclick="confirmarExclusao(${item.idRef})" title="Excluir"><i class="fa-regular fa-trash-can"></i></button>
                        </div>
                    `;
                }

                // 5. Tratamento de Descrição (Evita repetição e quebra textos bizarros)
                var descHtml = '';
                var descLimpa = item.subtitulo ? item.subtitulo.trim() : '';

                // Só mostra se tiver texto e se for DIFERENTE do título!
                if (descLimpa !== '' && descLimpa.toLowerCase() !== tituloExibicao.toLowerCase()) {
                    descHtml = `<div class="text-muted mt-1" style="font-size: 0.8rem; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis; word-break: break-word;">${descLimpa}</div>`;
                }

                // 6. Monta o HTML com estrutura flexível
                html += `
                    <div class="list-group-item d-flex justify-content-between align-items-start py-3 border-0 border-bottom px-2 bg-transparent">
                        
                        <div class="d-flex align-items-start w-100 pe-3" style="min-width: 0;">
                            <div class="rounded-circle d-flex align-items-center justify-content-center flex-shrink-0 me-3 ${bgIcone} ${corIcone} ${opacidadeIcone}" style="width: 40px; height: 40px; margin-top: 2px;">
                                <i class="fa-solid ${icone} fs-6"></i>
                            </div>
                            
                            <div class="flex-grow-1" style="min-width: 0;">
                                <h6 class="mb-1 fw-bold ${classeTitulo} text-truncate" title="${tituloExibicao}">${tituloExibicao}</h6>
                                
                                <div class="d-flex flex-wrap gap-1 align-items-center">
                                    <span class="badge bg-light text-secondary border" style="font-size: 0.65rem;">${item.tipo}</span>
                                    ${badgeStatus}
                                </div>
                                
                                ${descHtml}
                            </div>
                        </div>
                        
                        <div class="ms-auto flex-shrink-0 mt-1">
                            ${botoesAcao}
                        </div>
                        
                    </div>
                `;
            });
            
            // ESSAS DUAS LINHAS ESTAVAM FALTANDO NO SEU CÓDIGO!
            $('#listaDetalhes').html(html);
        }
    });
}

/**
 * 3. Ver Mensagem
 */
function verMensagem(id) {
    if (!id) return;
    fecharModalLista();

    setTimeout(() => {
        var el = document.getElementById('modalLeitura');
        var modalLeitura = new bootstrap.Modal(el);
        modalLeitura.show();

        $('#modalConteudo').html('<div class="text-center py-4"><div class="spinner-border text-primary"></div></div>');
        $('#modalAssunto').text('Carregando...');

        $.get("/mensagens/detalhes/" + id, function(data) {
            $('#modalAssunto').text(data.assunto);
            $('#modalGrupo').text(data.nomeGrupoDestino || 'Destinatário Único');

            if (data.dataEnvio) {
                let dt = new Date(data.dataEnvio);
                $('#modalData').text(dt.toLocaleDateString('pt-BR') + ' ' + dt.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }));
            }

            if (data.nomesAnexos) {
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
 * 4. Editar Evento
 */
function editarEvento(id) {
    if (!id) return;
    fecharModalLista();

    setTimeout(() => {
        $.get('/administrativo/agenda/buscar/' + id, function(evento) {
            $('#inputId').val(evento.id);
            $('#inputTitulo').val(evento.titulo);
            $('#modalNovoEvento textarea[name="descricao"]').val(evento.descricao);

            if (evento.dataHora) {
                let partes = evento.dataHora.split('T');
                $('#tempData').val(partes[0]);
                $('#tempHora').val(partes[1].substring(0, 5));
                $('#inputDataHoraCompleta').val(evento.dataHora);
            }

            $('input[name="tipo"]').prop('checked', false);
            let radio = $(`input[name="tipo"][value="${evento.tipo}"]`);
            if (radio.length > 0) radio.prop('checked', true);
            else $('#tipoTarefa').prop('checked', true);

            alternarModoCriacao(false);

            var modalNovo = new bootstrap.Modal(document.getElementById('modalNovoEvento'));
            modalNovo.show();
        }).fail(function() {
            alert("Erro ao buscar dados do evento.");
            var el = document.getElementById('modalDetalhesDia');
            if (el) new bootstrap.Modal(el).show();
        });
    }, 150);
}

/**
 * 5. Excluir Evento
 */
function confirmarExclusao(id) {
    fecharModalLista();

    Swal.fire({
        title: 'Tem certeza?', text: "Deseja remover este item da agenda?", icon: 'warning',
        showCancelButton: true, confirmButtonColor: '#d33', cancelButtonColor: '#6c757d',
        confirmButtonText: 'Sim, excluir', cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: '/administrativo/agenda/remover/' + id, type: 'GET',
                success: function() {
                    Swal.fire({ title: 'Excluído!', icon: 'success', timer: 1500, showConfirmButton: false })
                        .then(() => location.reload());
                }
            });
        } else {
            var el = document.getElementById('modalDetalhesDia');
            if (el) new bootstrap.Modal(el).show();
        }
    });
}

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

// ==========================================================
// SINCRONIZAÇÃO DE DATA E HORA (LADO DE FORA DE FORMA CORRETA)
// ==========================================================
$(document).ready(function() {

    // Função que une a Data e Hora temporárias no formato ISO pro Spring
    function atualizarDataHoraOculta() {
        var data = $('#tempData').val();
        var hora = $('#tempHora').val();

        if (data && hora && data !== "" && hora !== "") {
            var dataHoraUnificada = data + 'T' + hora;

            // Força a atualização do input hidden que vai pro Java
            $('#inputDataHoraCompleta').val(dataHoraUnificada);
            $('input[name="dataHora"]').val(dataHoraUnificada);
        }
    }

    // Escuta qualquer mudança nos inputs visíveis
    $('#tempData, #tempHora').on('change input blur', function() {
        atualizarDataHoraOculta();
    });

    // Como garantia de segurança, antes de disparar o submit do formulário:
    $('#modalNovoEvento form').on('submit', function(e) {
        atualizarDataHoraOculta();

        var valorOculto = $('input[name="dataHora"]').val();
        if (!valorOculto || !valorOculto.includes('T')) {
            console.warn("Forçando preenchimento de fallback na data/hora");
            var fallbackData = $('#tempData').val() || new Date().toISOString().split('T')[0];
            var fallbackHora = $('#tempHora').val() || "09:00";
            $('input[name="dataHora"]').val(fallbackData + 'T' + fallbackHora);
        }
    });
});

// ==========================================================
// LÓGICA DE FILTROS E PESQUISA DA AGENDA
// ==========================================================
$(document).ready(function() {

    function aplicarFiltrosAgenda() {
        var textoFiltro = $('.filter-btn.active').text().trim().toLowerCase();
        var termoBusca = $('#buscaAgenda').val().toLowerCase();

        var filtroClasse = "TODOS";
        if (textoFiltro.includes("disparos")) filtroClasse = "ENVIO";
        else if (textoFiltro.includes("reuniões")) filtroClasse = "REUNIAO";
        else if (textoFiltro.includes("urgentes")) filtroClasse = "IMPORTANTE";
        else if (textoFiltro.includes("tarefas")) filtroClasse = "TAREFA";

        $('.event-badge').each(function() {
            var badge = $(this);
            var tituloEvento = badge.text().toLowerCase();

            var atendeFiltroBotao = (filtroClasse === "TODOS" || badge.hasClass(filtroClasse));
            var atendeFiltroBusca = (termoBusca === "" || tituloEvento.includes(termoBusca));

            if (atendeFiltroBotao && atendeFiltroBusca) {
                badge.fadeIn(150);
            } else {
                badge.fadeOut(150);
            }
        });
    }

    $('.filter-btn').on('click', function() {
        $('.filter-btn').removeClass('active');
        $(this).addClass('active');
        aplicarFiltrosAgenda();
    });

    $('#buscaAgenda').on('input', function() {
        aplicarFiltrosAgenda();
    });

});

// ==========================================================
// MARCAÇÃO DE TAREFAS CONCLUÍDAS
// ==========================================================
function concluirTarefa(idTarefa, elemento) {
    if (idTarefa === 0) {
        Swal.fire('Atenção', 'Esta tarefa é antiga e está sem ID. Atualize a página (F5) ou crie uma tarefa nova para testar.', 'warning');
        return;
    }

    var $btn = $(elemento);
    var $icone = $btn.find('i');
    var $textos = $btn.closest('.mini-event-item').find('h6, p');

    if ($btn.hasClass('task-done')) return;

    $icone.removeClass('fa-regular fa-circle text-muted').addClass('fa-solid fa-circle-check text-success');
    $textos.css({ 'text-decoration': 'line-through', 'opacity': '0.5', 'transition': 'all 0.3s' });
    $btn.addClass('task-done');

    $.get('/administrativo/agenda/concluir/' + idTarefa)
        .done(function() {
            console.log("Tarefa " + idTarefa + " concluída com sucesso!");
        })
        .fail(function(jqXHR) {
            $icone.removeClass('fa-solid fa-circle-check text-success').addClass('fa-regular fa-circle text-muted');
            $textos.css({ 'text-decoration': 'none', 'opacity': '1' });
            $btn.removeClass('task-done');

            var msgErro = jqXHR.responseText || "Erro desconhecido no servidor";
            var status = jqXHR.status;

            Swal.fire('Ops!', 'O servidor recusou. Status: ' + status + '<br>Motivo: ' + msgErro, 'error');
        });
}
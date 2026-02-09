// --- agenda.js ---

// 1. Abre o modal de cadastro pré-preenchido
function abrirModalNovoEvento(dia, mes, ano) {
    var elDetalhes = document.getElementById('modalDetalhesDia');
    if (elDetalhes) {
        var modalDetalhes = bootstrap.Modal.getInstance(elDetalhes);
        if (modalDetalhes) modalDetalhes.hide();
    }

    var mesFmt = mes < 10 ? '0' + mes : mes;
    var diaFmt = dia < 10 ? '0' + dia : dia;
    var dataHoraISO = `${ano}-${mesFmt}-${diaFmt}T09:00`;

    $('#modalNovoEvento input[name="dataHora"]').val(dataHoraISO);
    $('#modalNovoEvento input[name="titulo"]').val('');
    $('#modalNovoEvento textarea[name="descricao"]').val('');
    $('#modalNovoEvento select[name="contato"]').val(''); 
    
    // Garante que a área de seleção esteja visível e reseta
    alternarModoCriacao(false);
    $('#tipoTarefa').prop('checked', true);

    var modalNovo = new bootstrap.Modal(document.getElementById('modalNovoEvento'));
    modalNovo.show();
}

// 2. Abre o modal de detalhes
function verDetalhesDia(dia, mes, ano) {
    var dataVisual = dia + '/' + mes + '/' + ano;
    $('#modalTituloData').text(dataVisual);
    
    var mesFormatado = mes < 10 ? '0' + mes : mes;
    var diaFormatado = dia < 10 ? '0' + dia : dia;
    var dataISO = ano + '-' + mesFormatado + '-' + diaFormatado;

    $('#btnAdicionarNestaData').attr('onclick', `abrirModalNovoEvento(${dia}, ${mes}, ${ano})`);
    $('#listaDetalhes').html('<div class="text-center py-3"><div class="spinner-border text-primary spinner-border-sm"></div></div>');
    $('#msgVazio').addClass('d-none');
    
    var modal = new bootstrap.Modal(document.getElementById('modalDetalhesDia'));
    modal.show();

    $.get('/administrativo/agenda/detalhes?data=' + dataISO, function(dados) {
        var html = '';
        if (!dados || dados.length === 0) {
            $('#listaDetalhes').empty();
            $('#msgVazio').removeClass('d-none');
        } else {
            $('#msgVazio').addClass('d-none');
            dados.forEach(function(item) {
                var icone = 'fa-circle';
                var cor = 'text-secondary';
                var acao = '';

                // Mapeamento de Ícones e Cores
                if (item.tipo === 'NIVER') { icone = 'fa-cake-candles'; cor = 'text-success'; } 
                else if (item.tipo === 'ENVIO') { 
                    icone = 'fa-paper-plane'; cor = 'text-primary'; 
                    acao = '<button onclick="verMensagem(' + item.idRef + ')" class="btn btn-sm btn-outline-primary ms-auto"><i class="fa-solid fa-eye"></i></button>';
                } 
                else if (item.tipo === 'FERIADO') { icone = 'fa-calendar-check'; cor = 'text-danger'; }
                else if (item.tipo === 'REUNIAO') { icone = 'fa-users'; cor = 'text-primary'; }
                else if (item.tipo === 'IMPORTANTE') { icone = 'fa-triangle-exclamation'; cor = 'text-danger'; }
                else if (item.tipo === 'TAREFA') { icone = 'fa-list-check'; cor = 'text-info'; }
                else {
                    // Tipos Personalizados (Fallback genérico se não tiver mapeado)
                    icone = 'fa-tag'; 
                    cor = 'text-dark'; 
                }

                html += `
                    <div class="list-group-item d-flex align-items-center p-3">
                        <div class="rounded-circle bg-light p-2 me-3 ${cor}">
                            <i class="fa-solid ${icone}"></i>
                        </div>
                        <div>
                            <h6 class="mb-0 fw-bold small text-dark">${item.titulo}</h6>
                            <small class="text-muted" style="font-size: 0.75rem;">${item.subtitulo || ''}</small>
                        </div>
                        ${acao}
                    </div>
                `;
            });
            $('#listaDetalhes').html(html);
        }
    });
}

// 3. Ver Mensagem
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
        $('#modalConteudo img').addClass('img-fluid rounded'); 
    });
}

// --- NOVAS FUNÇÕES (Faltavam estas) ---

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
        alert("Por favor, dê um nome ao tipo.");
        return;
    }

    const payload = {
        nome: nome,
        icone: icone,
        corHex: cor
    };

    // Efeito visual de loading no botão
    const btn = event.target; // Pega o botão clicado
    const textoOriginal = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
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
            alert('Erro ao criar tipo. Verifique se o Backend está rodando (TipoEventoControle).');
        },
        complete: function() {
            btn.innerHTML = textoOriginal;
            btn.disabled = false;
        }
    });
}

// Inicialização
document.addEventListener('DOMContentLoaded', function() {
    const params = new URLSearchParams(window.location.search);
    if (params.get('acao') === 'novoEvento') {
        // ... lógica existente ...
    }
    
    // Injeta estilo de animação se não existir
    if (!document.getElementById('styleFadeIn')) {
        document.head.insertAdjacentHTML("beforeend", `<style id="styleFadeIn">.fade-in { animation: fadeIn 0.3s ease-in-out; } @keyframes fadeIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }</style>`);
    }
});
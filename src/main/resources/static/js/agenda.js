// --- agenda.js ---

// 1. Abre o modal de cadastro pré-preenchido
function abrirModalNovoEvento(dia, mes, ano) {
    // Fecha o modal de detalhes atual (se estiver aberto)
    var elDetalhes = document.getElementById('modalDetalhesDia');
    if (elDetalhes) {
        var modalDetalhes = bootstrap.Modal.getInstance(elDetalhes);
        if (modalDetalhes) {
            modalDetalhes.hide();
        }
    }

    // Formata a data para o padrão do input datetime-local (YYYY-MM-DDTHH:mm)
    var mesFmt = mes < 10 ? '0' + mes : mes;
    var diaFmt = dia < 10 ? '0' + dia : dia;
    
    // Define um horário padrão (ex: 09:00) para facilitar
    var dataHoraISO = `${ano}-${mesFmt}-${diaFmt}T09:00`;

    // Preenche o campo de data no formulário de novo evento
    $('#modalNovoEvento input[name="dataHora"]').val(dataHoraISO);
    
    // Limpa os outros campos
    $('#modalNovoEvento input[name="titulo"]').val('');
    $('#modalNovoEvento textarea[name="descricao"]').val('');
    $('#modalNovoEvento select[name="tipo"]').val('TAREFA');

    // Abre o modal de cadastro
    var modalNovo = new bootstrap.Modal(document.getElementById('modalNovoEvento'));
    modalNovo.show();
}

// 2. Abre o modal de detalhes e busca os dados no servidor
function verDetalhesDia(dia, mes, ano) {
    // Formata data para o título (Visual)
    var dataVisual = dia + '/' + mes + '/' + ano;
    $('#modalTituloData').text(dataVisual);
    
    // Formata data para a API (ISO: YYYY-MM-DD)
    var mesFormatado = mes < 10 ? '0' + mes : mes;
    var diaFormatado = dia < 10 ? '0' + dia : dia;
    var dataISO = ano + '-' + mesFormatado + '-' + diaFormatado;

    // Configura o botão "Adicionar" para abrir o modal na data certa
    $('#btnAdicionarNestaData').attr('onclick', `abrirModalNovoEvento(${dia}, ${mes}, ${ano})`);

    // Mostra carregando enquanto busca
    $('#listaDetalhes').html('<div class="text-center py-3"><div class="spinner-border text-primary spinner-border-sm"></div></div>');
    $('#msgVazio').addClass('d-none');
    
    var modal = new bootstrap.Modal(document.getElementById('modalDetalhesDia'));
    modal.show();

    // Chama o Back-end (Rota corrigida para /administrativo/agenda/detalhes)
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

                if (item.tipo === 'NIVER') {
                    icone = 'fa-cake-candles';
                    cor = 'text-success';
                } else if (item.tipo === 'ENVIO') {
                    icone = 'fa-paper-plane';
                    cor = 'text-primary';
                    // Botão para ver o e-mail enviado
                    acao = '<button onclick="verMensagem(' + item.idRef + ')" class="btn btn-sm btn-outline-primary ms-auto"><i class="fa-solid fa-eye"></i></button>';
                } else if (item.tipo === 'FERIADO') {
                    icone = 'fa-calendar-check';
                    cor = 'text-danger';
                } else {
                    // Tarefas, Reuniões, etc
                    icone = 'fa-check';
                    cor = 'text-info';
                }

                html += `
                    <div class="list-group-item d-flex align-items-center p-3">
                        <div class="rounded-circle bg-light p-2 me-3 ${cor}">
                            <i class="fa-solid ${icone}"></i>
                        </div>
                        <div>
                            <h6 class="mb-0 fw-bold small text-dark">${item.titulo}</h6>
                            <small class="text-muted" style="font-size: 0.75rem;">${item.subtitulo}</small>
                        </div>
                        ${acao}
                    </div>
                `;
            });
            
            $('#listaDetalhes').html(html);
        }
    }).fail(function() {
        $('#listaDetalhes').html('<div class="text-center text-danger py-3">Erro ao carregar dados.</div>');
    });
}

// 3. Função para ver detalhes de uma mensagem enviada (Modal Leitura)
function verMensagem(id) {
    // Fecha o modal de detalhes do dia se estiver aberto (opcional)
    // var modalDetalhes = bootstrap.Modal.getInstance(document.getElementById('modalDetalhesDia'));
    // if(modalDetalhes) modalDetalhes.hide();

    // Mostra loading no modal de leitura
    $('#modalConteudo').html('<div class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></div>');
    
    var modal = new bootstrap.Modal(document.getElementById('modalLeitura'));
    modal.show();

    // Busca dados no Backend
    $.get("/mensagens/detalhes/" + id, function(data) {
        $('#modalAssunto').text(data.assunto);
        $('#modalGrupo').text(data.nomeGrupoDestino || 'Destinatário Único');
        $('#modalData').text(new Date(data.dataEnvio).toLocaleString('pt-BR'));
        
        // Anexos
        if(data.nomesAnexos) {
            $('#modalAnexos').text(data.nomesAnexos);
            $('#areaAnexosModal').removeClass('d-none');
        } else {
            $('#areaAnexosModal').addClass('d-none');
        }
        
        var conteudoLimpo = data.conteudo; 
        $('#modalConteudo').html(conteudoLimpo);
        $('#modalConteudo img').addClass('img-fluid rounded'); 
    });
}

// 4. Inicialização ao carregar a página
document.addEventListener('DOMContentLoaded', function() {
    const params = new URLSearchParams(window.location.search);
    
    // Verifica se veio com ordem de "novoEvento" (ex: vindo de outra tela)
    if (params.get('acao') === 'novoEvento') {
        const titulo = params.get('titulo');
        
        var modalElement = document.getElementById('modalNovoEvento');
        if (modalElement) {
            var modal = new bootstrap.Modal(modalElement);
            modal.show();
            
            // Preenche o campo de título se existir
            var inputTitulo = modalElement.querySelector('input[name="titulo"]');
            if (inputTitulo && titulo) {
                inputTitulo.value = titulo;
            }
            
            // Limpa a URL para não reabrir ao atualizar a página
            const novaURL = window.location.pathname;
            window.history.replaceState({}, document.title, novaURL);
        }
    }
});
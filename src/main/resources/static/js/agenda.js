// --- agenda.js ---

// 1. NOVA FUNÇÃO: Abre o modal de cadastro pré-preenchido
function abrirModalNovoEvento(dia, mes, ano) {
    // Fecha o modal de detalhes atual (se estiver aberto)
    var elDetalhes = document.getElementById('modalDetalhesDia');
    var modalDetalhes = bootstrap.Modal.getInstance(elDetalhes);
    if (modalDetalhes) {
        modalDetalhes.hide();
    }

    // Formata a data para o padrão do input datetime-local (YYYY-MM-DDTHH:mm)
    var mesFmt = mes < 10 ? '0' + mes : mes;
    var diaFmt = dia < 10 ? '0' + dia : dia;
    
    // Define um horário padrão (ex: 09:00) para facilitar
    var dataHoraISO = `${ano}-${mesFmt}-${diaFmt}T09:00`;

    // Preenche o campo de data no formulário de novo evento
    // Seleciona o input name="dataHora" dentro do modalNovoEvento
    $('#modalNovoEvento input[name="dataHora"]').val(dataHoraISO);
    
    // Limpa os outros campos para não trazer lixo de cadastros anteriores
    $('#modalNovoEvento input[name="titulo"]').val('');
    $('#modalNovoEvento textarea[name="descricao"]').val('');
    $('#modalNovoEvento select[name="tipo"]').val('TAREFA');

    // Abre o modal de cadastro
    var modalNovo = new bootstrap.Modal(document.getElementById('modalNovoEvento'));
    modalNovo.show();
}

// 2. FUNÇÃO ATUALIZADA: Abre o modal de detalhes e configura o botão "Adicionar"
function verDetalhesDia(dia, mes, ano) {
    // Formata data para o título (Visual)
    var dataVisual = dia + '/' + mes + '/' + ano;
    $('#modalTituloData').text(dataVisual);
    
    // Formata data para a API (ISO: YYYY-MM-DD)
    var mesFormatado = mes < 10 ? '0' + mes : mes;
    var diaFormatado = dia < 10 ? '0' + dia : dia;
    var dataISO = ano + '-' + mesFormatado + '-' + diaFormatado;

    // --- IMPORTANTE: VINCULA O CLIQUE DO BOTÃO À DATA ATUAL ---
    // Isso garante que ao clicar em "Adicionar" no modal de detalhes,
    // ele saiba exatamente qual dia enviar para o modal de cadastro.
    $('#btnAdicionarNestaData').attr('onclick', `abrirModalNovoEvento(${dia}, ${mes}, ${ano})`);
    // -----------------------------------------------------------

    // Limpa e mostra carregando
    $('#listaDetalhes').html('<div class="text-center py-3"><div class="spinner-border text-primary spinner-border-sm"></div></div>');
    $('#msgVazio').addClass('d-none');
    
    var modal = new bootstrap.Modal(document.getElementById('modalDetalhesDia'));
    modal.show();

    // Chama o Back-end
    $.get('/agenda/detalhes?data=' + dataISO, function(dados) {
        var html = '';
        
        if (dados.length === 0) {
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
                   acao = '<button class="btn btn-sm btn-outline-primary ms-auto" onclick="verMensagem(' + item.idRef + ')"><i class="fa-solid fa-eye"></i></button>';
                } else if (item.tipo === 'FERIADO') {
                    icone = 'fa-calendar-check';
                    cor = 'text-danger';
                } else {
                    // Ícone padrão para tarefas manuais
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
    });
	function verMensagem(id) {
	           // Fecha o modal de detalhes do dia para não ficar um em cima do outro (opcional)
	           // var modalDetalhes = bootstrap.Modal.getInstance(document.getElementById('modalDetalhesDia'));
	           // if(modalDetalhes) modalDetalhes.hide();

	           // Mostra loading
	           $('#modalConteudo').html('<div class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></div>');
	           
	           var modal = new bootstrap.Modal(document.getElementById('modalLeitura'));
	           modal.show();

	           // Busca dados no Backend
	           $.get("/mensagens/detalhes/" + id, function(data) {
	               $('#modalAssunto').text(data.assunto);
	               $('#modalGrupo').text(data.nomeGrupoDestino);
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
}
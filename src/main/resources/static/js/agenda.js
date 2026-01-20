// --- agenda.js ---

// Função para abrir o modal ao clicar no dia
// (Agora está no escopo global, acessível pelo onclick do HTML)
function verDetalhesDia(dia, mes, ano) {
    // Formata data para o título (Visual)
    var dataVisual = dia + '/' + mes + '/' + ano;
    $('#modalTituloData').text(dataVisual);
    
    // Formata data para a API (ISO: YYYY-MM-DD)
    var mesFormatado = mes < 10 ? '0' + mes : mes;
    var diaFormatado = dia < 10 ? '0' + dia : dia;
    var dataISO = ano + '-' + mesFormatado + '-' + diaFormatado;

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
                    acao = '<a href="/editarContatos/' + item.idRef + '" class="btn btn-sm btn-outline-success ms-auto"><i class="fa-solid fa-envelope"></i></a>';
                } else if (item.tipo === 'ENVIO') {
                    icone = 'fa-paper-plane';
                    cor = 'text-primary';
                    acao = '<button class="btn btn-sm btn-outline-primary ms-auto"><i class="fa-solid fa-eye"></i></button>';
                } else if (item.tipo === 'FERIADO') {
                    icone = 'fa-calendar-check';
                    cor = 'text-danger';
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
}


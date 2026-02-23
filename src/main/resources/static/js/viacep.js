document.addEventListener('DOMContentLoaded', function() {

    // ==========================================
    // 1. LÓGICA DO CEP (Busca + Máscara + Spinner)
    // ==========================================
    const cepInput = document.getElementById('cep');
    
    // Função para exibir o Modal de Erro do CEP
    function mostrarErroCep(mensagem) {
        const modalElement = document.getElementById('erroCepModal');
        if (modalElement) {
            document.getElementById('mensagemErroCep').innerText = mensagem;
            const modal = new bootstrap.Modal(modalElement);
            modal.show();
        } else {
            alert(mensagem);
        }
    }

    if (cepInput) {
        
        function pesquisarCep(valor) {
            var cep = valor.replace(/\D/g, ''); 
            const iconeBusca = document.querySelector('#btnBuscarCep i'); // Seleciona o ícone da lupa

            if (cep !== "") {
                var validacep = /^[0-9]{8}$/; 

                if(validacep.test(cep)) {
                    // Preenche com "..." enquanto carrega
                    document.getElementById('rua').value = "...";
                    document.getElementById('bairro').value = "...";
                    document.getElementById('cidade').value = "...";

                    // Ativa o spinner de carregamento
                    if(iconeBusca) {
                        iconeBusca.className = 'fa-solid fa-spinner fa-spin text-primary';
                    }

                    // Utilizando BrasilAPI
                    fetch('https://brasilapi.com.br/api/cep/v1/' + cep)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('CEP não encontrado ou erro de rede');
                        }
                        return response.json();
                    })
                    .then(data => {
                        document.getElementById('rua').value = data.street || '';
                        document.getElementById('bairro').value = data.neighborhood || '';
                        document.getElementById('cidade').value = data.city || '';
                        document.getElementById('estado').value = data.state || '';
                        
                        document.getElementById('numero').focus(); // Foca no número
                    })
                    .catch(error => {
                        console.error(error);
                        mostrarErroCep("O CEP digitado não foi encontrado. Verifique os números e tente novamente.");
                        limparFormularioCep();
                    })
                    .finally(() => {
                        // Desativa o spinner e volta a lupa (independente de dar erro ou sucesso)
                        if(iconeBusca) {
                            iconeBusca.className = 'fa-solid fa-magnifying-glass text-muted';
                        }
                    });

                } else {
                    mostrarErroCep("Formato de CEP inválido. Por favor, digite 8 números.");
                    limparFormularioCep();
                }
            } else {
                limparFormularioCep();
            }
        }

        // A. Busca na API ao clicar fora (Evento Blur)
        cepInput.addEventListener('blur', function() {
            pesquisarCep(this.value);
        });

        // B. Máscara Visual e Busca Automática (Evento Input)
        cepInput.addEventListener('input', function (e) {
            var value = e.target.value.replace(/\D/g, ''); 
            var formattedValue = value;

            if (value.length > 5) {
                formattedValue = value.replace(/^(\d{5})(\d{0,3}).*/, '$1-$2');
            }
            
            e.target.value = formattedValue;

            // Aciona a API automaticamente ao digitar os 8 números
            if (value.length === 8) {
                pesquisarCep(value);
            }
        });

        // C. Busca manual ao clicar no ícone da lupa
        const btnBuscaCep = document.getElementById('btnBuscarCep');
        if (btnBuscaCep) {
            btnBuscaCep.addEventListener('click', function() {
                pesquisarCep(cepInput.value);
            });
        }
    }

    // ==========================================
    // 2. MÁSCARA DE TELEFONE
    // ==========================================
    const telefoneInput = document.getElementById('telefone');
    
    if(telefoneInput){
        telefoneInput.addEventListener('input', function (e) {
            var value = e.target.value.replace(/\D/g, '');
            var formattedValue = "";

            if (value.length > 10) {
                formattedValue = value.replace(/^(\d{2})(\d{5})(\d{4}).*/, '($1) $2-$3');
            } else if (value.length > 5) {
                formattedValue = value.replace(/^(\d{2})(\d{4})(\d{0,4}).*/, '($1) $2-$3');
            } else if (value.length > 2) {
                formattedValue = value.replace(/^(\d{2})(\d{0,5}).*/, '($1) $2');
            } else {
                formattedValue = value.replace(/^(\d*)/, '($1');
            }
            
            e.target.value = formattedValue;
        });
    }

    // ==========================================
    // 3. BLOQUEIO DE NÚMEROS NEGATIVOS
    // ==========================================
    const numeroInput = document.getElementById('numero');
    
    if(numeroInput){
        numeroInput.addEventListener('keydown', function(e) {
            if(e.key === '-' || e.key === 'e') {
                e.preventDefault();
            }
        });
        
        numeroInput.addEventListener('input', function(e) {
            this.value = this.value.replace(/[^0-9]/g, '');
        });
    }
});

function limparFormularioCep() {
    document.getElementById('rua').value = "";
    document.getElementById('bairro').value = "";
    document.getElementById('cidade').value = "";
    document.getElementById('estado').value = "";
}
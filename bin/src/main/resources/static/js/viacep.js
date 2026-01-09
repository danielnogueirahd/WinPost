document.addEventListener('DOMContentLoaded', function() {

    // ==========================================
    // 1. LÓGICA DO CEP (Busca + Máscara)
    // ==========================================
    const cepInput = document.getElementById('cep');

    if (cepInput) {
        // A. Busca na API (Evento Blur - Quando sai do campo)
        cepInput.addEventListener('blur', function() {
            // Remove máscara (deixa apenas números) para consultar a API
            var cep = this.value.replace(/\D/g, ''); 

            if (cep !== "") {
                var validacep = /^[0-9]{8}$/; 

                if(validacep.test(cep)) {
                    // Preenche com "..." enquanto carrega
                    document.getElementById('rua').value = "...";
                    document.getElementById('bairro').value = "...";
                    document.getElementById('cidade').value = "...";
                    document.getElementById('estado').value = "...";

                    fetch('https://viacep.com.br/ws/'+ cep +'/json/')
                    .then(response => response.json())
                    .then(data => {
                        if(!("erro" in data)) {
                            document.getElementById('rua').value = data.logradouro;
                            document.getElementById('bairro').value = data.bairro;
                            document.getElementById('cidade').value = data.localidade;
                            document.getElementById('estado').value = data.uf;
                            
                            document.getElementById('numero').focus(); // Foca no número
                        } else {
                            alert("CEP não encontrado.");
                            limparFormularioCep();
                        }
                    })
                    .catch(error => {
                        console.error(error);
                        alert("Erro ao buscar CEP.");
                        limparFormularioCep();
                    });
                } else {
                    alert("Formato de CEP inválido.");
                    limparFormularioCep();
                }
            } else {
                limparFormularioCep();
            }
        });

        // B. Máscara Visual (Evento Input - Enquanto digita)
        // Formato: 00000-000
        cepInput.addEventListener('input', function (e) {
            var value = e.target.value.replace(/\D/g, ''); // Remove letras
            var formattedValue = value;

            if (value.length > 5) {
                formattedValue = value.replace(/^(\d{5})(\d{0,3}).*/, '$1-$2');
            }
            
            e.target.value = formattedValue;
        });
    }

    // ==========================================
    // 2. MÁSCARA DE TELEFONE
    // ==========================================
    const telefoneInput = document.getElementById('telefone');
    
    if(telefoneInput){
        telefoneInput.addEventListener('input', function (e) {
            var value = e.target.value.replace(/\D/g, '');
            var formattedValue = "";

            // Formata (00) 00000-0000 ou (00) 0000-0000
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
        // Previne digitar sinal de menos (-) e 'e' (exponencial)
        numeroInput.addEventListener('keydown', function(e) {
            if(e.key === '-' || e.key === 'e') {
                e.preventDefault();
            }
        });
        
        // Garante que só fique números positivos caso cole texto
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
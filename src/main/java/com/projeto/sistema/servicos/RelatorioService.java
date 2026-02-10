package com.projeto.sistema.servicos;

import java.awt.Color;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.springframework.stereotype.Service;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.projeto.sistema.modelos.Contatos;

@Service
public class RelatorioService {

    public ByteArrayInputStream gerarRelatorioContatos(List<Contatos> contatos) {
        // Define documento em Paisagem (A4 Deitado)
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph titulo = new Paragraph("Relatório Detalhado de Contatos", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new Paragraph(" "));

            // MUDANÇA: Tabela agora tem 7 colunas (removemos a Data)
            PdfPTable tabela = new PdfPTable(7);
            tabela.setWidthPercentage(100);
            
            // MUDANÇA: Removido o último valor (1.2f) referente à data
            tabela.setWidths(new float[]{0.5f, 2f, 2f, 1.5f, 2.5f, 1.5f, 0.8f});

            // MUDANÇA: Removido "Data" do cabeçalho
            adicionarCabecalho(tabela, "ID", "Nome", "Email", "Telefone", "Endereço", "Cidade", "UF");

            // Dados
            Font fontDados = FontFactory.getFont(FontFactory.HELVETICA, 10);
            
            // Loop simplificado (sem formatador de data)
            for (Contatos contato : contatos) {
                // Tratamento básico para evitar NULL no endereço
                String rua = contato.getRua() != null ? contato.getRua() : "";
                String numero = contato.getNumero() != null ? String.valueOf(contato.getNumero()) : "S/N";
                String cep = contato.getCep() != null ? contato.getCep() : "";
                
                String endereco = rua + ", " + numero + " - CEP: " + cep;

                // MUDANÇA: Removido o argumento de dataCadastro no final
                adicionarCelulas(tabela, fontDados,
                        String.valueOf(contato.getId()),
                        contato.getNome(),
                        contato.getEmail(),
                        contato.getTelefone(),
                        endereco,
                        contato.getCidade(),
                        contato.getEstado()
                );
            }

            document.add(tabela);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
    
 // Adicione este método dentro da classe RelatorioService

    public ByteArrayInputStream gerarEtiquetas(List<Contatos> contatos) {
        // 1. Cria o documento A4 (Retrato é melhor para etiquetas)
        // Margens ajustadas (30 nas laterais, 20 topo/base) para evitar corte na impressora
        Document document = new Document(PageSize.A4, 30, 30, 20, 20);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 2. Cria uma tabela com 3 colunas (padrão comum de etiquetas)
            PdfPTable tabela = new PdfPTable(3);
            tabela.setWidthPercentage(100);
            tabela.setWidths(new float[]{1f, 1f, 1f}); // Colunas com larguras iguais
            
            // Fontes para a etiqueta
            Font fontNome = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontEndereco = FontFactory.getFont(FontFactory.HELVETICA, 9);

            for (Contatos contato : contatos) {
                // 3. Monta o texto do endereço
                String rua = contato.getRua() != null ? contato.getRua() : "";
                String numero = contato.getNumero() != null ? String.valueOf(contato.getNumero()) : "S/N";
                String bairro = contato.getBairro() != null ? contato.getBairro() : "";
                String cidade = contato.getCidade() != null ? contato.getCidade() : "";
                String uf = contato.getEstado() != null ? contato.getEstado() : "";
                String cep = contato.getCep() != null ? "CEP: " + contato.getCep() : "";

                // Formatação do texto dentro da etiqueta
                // Ex:
                // JOÃO SILVA
                // Rua das Flores, 123
                // Centro - São Paulo/SP
                // CEP: 12345-000
                String textoEtiqueta = rua + ", " + numero + "\n" +
                                       bairro + " - " + cidade + "/" + uf + "\n" +
                                       cep;

                // 4. Cria a célula (O "adesivo")
                PdfPCell cell = new PdfPCell();
                
                // Adiciona o Nome em Negrito
                cell.addElement(new Paragraph(contato.getNome().toUpperCase(), fontNome));
                // Adiciona o Endereço
                cell.addElement(new Paragraph(textoEtiqueta, fontEndereco));

                // Estilização da célula
                cell.setBorder(Rectangle.NO_BORDER); // Sem borda para imprimir (mude para BOX para testar)
                cell.setPadding(10); // Espaço interno para o texto não colar na borda
                cell.setFixedHeight(85f); // Altura fixa aproximada de uma etiqueta (ajuste conforme necessário)
                
                // Adiciona à tabela
                tabela.addCell(cell);
            }

            // 5. Completa a linha com células vazias se necessário (para fechar a grade)
            tabela.completeRow();

            document.add(tabela);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // Métodos auxiliares
    private void adicionarCabecalho(PdfPTable tabela, String... colunas) {
        Font fontHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String coluna : colunas) {
            PdfPCell cell = new PdfPCell(new Phrase(coluna, fontHead));
            cell.setBackgroundColor(Color.BLUE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            tabela.addCell(cell);
        }
    }

    private void adicionarCelulas(PdfPTable tabela, Font font, String... dados) {
        for (String dado : dados) {
            PdfPCell cell = new PdfPCell(new Phrase(dado != null ? dado : "", font));
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(3);
            tabela.addCell(cell);
        }
    }
}
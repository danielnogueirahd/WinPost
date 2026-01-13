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
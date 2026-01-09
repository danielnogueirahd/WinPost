package com.projeto.sistema.servicos;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
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
        // MUDANÇA 1: PageSize.A4.rotate() coloca a folha deitada (Paisagem)
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

            // MUDANÇA 2: Tabela com 8 colunas para caber todos os dados importantes
            PdfPTable tabela = new PdfPTable(8);
            tabela.setWidthPercentage(100);
            // Ajuste fino das larguras das colunas
            tabela.setWidths(new float[]{0.5f, 2f, 2f, 1.5f, 2.5f, 1.5f, 0.8f, 1.2f});

            // Cabeçalho estilizado
            adicionarCabecalho(tabela, "ID", "Nome", "Email", "Telefone", "Endereço", "Cidade", "UF", "Data");

            // Dados
            Font fontDados = FontFactory.getFont(FontFactory.HELVETICA, 10);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Contatos c : contatos) {
                adicionarCelulas(tabela, fontDados,
                    String.valueOf(c.getId()),
                    c.getNome(),
                    c.getEmail(),
                    c.getTelefone(),
                    // Concatenando endereço para economizar espaço
                    c.getRua() + ", " + c.getNumero() + (c.getComplemento() != null ? " - " + c.getComplemento() : ""),
                    c.getCidade(),
                    c.getEstado(),
                    c.getDataCadastro() != null ? c.getDataCadastro().format(formatter) : "-"
                );
            }

            document.add(tabela);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // Métodos auxiliares para limpar o código
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
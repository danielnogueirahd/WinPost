package com.projeto.sistema.servicos;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import com.projeto.sistema.modelos.Contatos; // Import do seu modelo

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class EtiquetaService {

    public ByteArrayInputStream gerarPdfEtiquetas(List<Contatos> contatos, String modeloEtiqueta, int posicaoInicial) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int colunas;
        float alturaEtiqueta; // Altura em pontos
        
        // Define as regras de acordo com o papel Pimaco selecionado na tela
        if ("6182".equals(modeloEtiqueta)) {
            colunas = 2; 
            alturaEtiqueta = 96f; 
        } else if ("6081".equals(modeloEtiqueta)) {
            colunas = 2;
            alturaEtiqueta = 72f; 
        } else {
            // Padrão: 6180
            colunas = 3; 
            alturaEtiqueta = 72f; 
        }

        // Cria o documento A4 (Margens: Esquerda, Direita, Cima, Baixo)
        Document document = new Document(PageSize.A4, 15, 15, 35, 35);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Cria a tabela invisível que vai segurar as etiquetas
            PdfPTable table = new PdfPTable(colunas);
            table.setWidthPercentage(100);

            // Tipografia
            Font fonteNome = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font fonteEndereco = FontFactory.getFont(FontFactory.HELVETICA, 8);
            Font fonteCep = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);

            // A MÁGICA: Pular as etiquetas que já foram usadas na folha
            int celulasVazias = posicaoInicial - 1;
            for (int i = 0; i < celulasVazias; i++) {
                PdfPCell cellVazia = new PdfPCell(new Phrase(" "));
                cellVazia.setBorder(Rectangle.NO_BORDER); // Deixa invisível
                cellVazia.setFixedHeight(alturaEtiqueta);
                table.addCell(cellVazia);
            }

            // Preenche os dados dos contatos
            for (Contatos c : contatos) {
                
                String nomeFormatado = c.getNome() != null ? c.getNome().toUpperCase() : "SEM NOME";
                Paragraph pNome = new Paragraph(nomeFormatado, fonteNome);
                
                String enderecoTexto = (c.getRua() != null ? c.getRua() : "") + 
                                       (c.getNumero() != null ? ", " + c.getNumero() : "");
                if (c.getBairro() != null && !c.getBairro().isEmpty()) {
                    enderecoTexto += " - " + c.getBairro();
                }
                Paragraph pEndereco = new Paragraph(enderecoTexto, fonteEndereco);
                
                String cepTexto = "CEP: " + (c.getCep() != null ? c.getCep() : "Não informado") 
                                  + " - " + (c.getCidade() != null ? c.getCidade() : "") 
                                  + "/" + (c.getEstado() != null ? c.getEstado() : "");
                Paragraph pCep = new Paragraph(cepTexto, fonteCep);

                // Monta a célula (a etiqueta individual)
                PdfPCell cell = new PdfPCell();
                cell.addElement(pNome);
                cell.addElement(pEndereco);
                cell.addElement(pCep);
                
                // Formatação do adesivo
                cell.setBorder(Rectangle.NO_BORDER); // Troque para Rectangle.BOX se quiser ver a linha delimitadora para testar
                cell.setFixedHeight(alturaEtiqueta);
                cell.setPaddingLeft(10f);
                cell.setPaddingTop(10f);

                table.addCell(cell);
            }

            // Garante que a tabela feche o ciclo corretamente
            table.completeRow();
            
            document.add(table);

        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
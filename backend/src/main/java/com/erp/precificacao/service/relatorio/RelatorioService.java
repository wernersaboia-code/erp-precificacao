package com.erp.precificacao.service.relatorio;

import com.erp.precificacao.model.Produto;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RelatorioService {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Gera relatório em formato CSV
     */
    public static String gerarCSV(List<Produto> produtos) throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
                "Nome", "Categoria", "Quantidade", "Preço Compra", "Preço Venda",
                "Lucro Bruto/Un", "Lucro Mensal", "Receita"
        );

        CSVPrinter csvPrinter = new CSVPrinter(sw, csvFormat);

        for (Produto p : produtos) {
            csvPrinter.printRecord(
                    p.getNome(),
                    p.getCategoria(),
                    p.getQuantidadeEstimada(),
                    String.format("%.2f", p.getPrecoCusto()),
                    String.format("%.2f", p.getPrecoIdeal() != null ? p.getPrecoIdeal() : 0),
                    String.format("%.2f", p.getLucroBrutoPorUnidade() != null ? p.getLucroBrutoPorUnidade() : 0),
                    String.format("%.2f", p.getLucroMensal() != null ? p.getLucroMensal() : 0),
                    String.format("%.2f", p.getReceita() != null ? p.getReceita() : 0)
            );
        }

        csvPrinter.flush();
        return sw.toString();
    }

    /**
     * Gera relatório em formato TXT legível
     */
    public static String gerarTXT(List<Produto> produtos) {
        StringBuilder sb = new StringBuilder();

        sb.append("╔════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║           RELATÓRIO DE PRECIFICAÇÃO E ANÁLISE                      ║\n");
        sb.append("║                                                                    ║\n");
        sb.append("║  Data: ").append(LocalDate.now().format(dateFormatter)).append("\n");
        sb.append("║  Total de Produtos: ").append(produtos.size()).append("\n");
        sb.append("╚════════════════════════════════════════════════════════════════════╝\n\n");

        // Relatório por produto
        sb.append("DETALHES POR PRODUTO:\n");
        sb.append("════════════════════════════════════════════════════════════════════\n\n");

        int contador = 1;
        for (Produto p : produtos) {
            sb.append(String.format("%d. %s\n", contador++, p.getNome()));
            sb.append(String.format("   Categoria: %s\n", p.getCategoria()));
            sb.append(String.format("   Quantidade Estimada: %d unidades\n", p.getQuantidadeEstimada()));
            sb.append(String.format("   Preço de Compra: R$ %.2f\n", p.getPrecoCusto()));
            sb.append(String.format("   Preço de Venda Ideal: R$ %.2f\n", p.getPrecoIdeal() != null ? p.getPrecoIdeal() : 0));
            sb.append(String.format("   Lucro Bruto por Unidade: R$ %.2f\n", p.getLucroBrutoPorUnidade() != null ? p.getLucroBrutoPorUnidade() : 0));
            sb.append(String.format("   Lucro Mensal: R$ %.2f\n", p.getLucroMensal() != null ? p.getLucroMensal() : 0));
            sb.append(String.format("   Receita Mensal: R$ %.2f\n", p.getReceita() != null ? p.getReceita() : 0));
            sb.append("\n");
        }

        // Resumo financeiro
        BigDecimal receitaTotal = BigDecimal.ZERO;
        BigDecimal lucroTotal = BigDecimal.ZERO;

        for (Produto p : produtos) {
            if (p.getReceita() != null) {
                receitaTotal = receitaTotal.add(p.getReceita());
            }
            if (p.getLucroBrutoPorUnidade() != null) {
                lucroTotal = lucroTotal.add(
                        p.getLucroBrutoPorUnidade().multiply(new java.math.BigDecimal(p.getQuantidadeEstimada()))
                );
            }
        }

        sb.append("════════════════════════════════════════════════════════════════════\n");
        sb.append("RESUMO FINANCEIRO:\n");
        sb.append("════════════════════════════════════════════════════════════════════\n");
        sb.append(String.format("Receita Total Mensal: R$ %.2f\n", receitaTotal));
        sb.append(String.format("Lucro Total Mensal: R$ %.2f\n", lucroTotal));
        sb.append("\n════════════════════════════════════════════════════════════════════\n");

        return sb.toString();
    }
}
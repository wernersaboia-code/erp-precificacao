package com.erp.precificacao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PrecificacaoService {

    private static final int SCALE = 2;

    public static BigDecimal calcularPrecoIdeal(
            BigDecimal custoCusto,
            BigDecimal margemDesejada,
            BigDecimal impostos) {

        BigDecimal denominador = BigDecimal.ONE
                .subtract(margemDesejada)
                .subtract(impostos);

        if (denominador.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Margem + Impostos nÃ£o podem ser >= 100%");
        }

        return custoCusto.divide(denominador, SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcularMargemBruta(
            BigDecimal precoIdeal,
            BigDecimal custoCusto) {

        return precoIdeal.subtract(custoCusto).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcularPontoEquilibrio(
            BigDecimal custoFixoTotal,
            BigDecimal margemBruta) {

        if (margemBruta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Margem bruta deve ser positiva");
        }

        return custoFixoTotal.divide(margemBruta, SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcularLucroMensal(
            BigDecimal margemBruta,
            Integer quantidadeEstimada) {

        return margemBruta
                .multiply(new BigDecimal(quantidadeEstimada))
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcularReceita(
            BigDecimal precoIdeal,
            Integer quantidadeEstimada) {

        return precoIdeal
                .multiply(new BigDecimal(quantidadeEstimada))
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o percentual com base em dois valores
     */
    public static BigDecimal calcularPercentual(BigDecimal valor, BigDecimal base) {
        if (base.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return valor.divide(base, SCALE, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
    }

    /**
     * Calcula custo fixo por unidade
     * FÃ³rmula: (Custo Fixo Mensal * PreÃ§o Compra) / SomatÃ³rio(PreÃ§o Compra * Quantidade)
     */
    public static BigDecimal calcularCustoFixoPorUnidade(
            BigDecimal custoFixoMensal,
            BigDecimal precoCusto,
            BigDecimal somatorioCustosQuantidades) {

        if (somatorioCustosQuantidades.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return custoFixoMensal
                .multiply(precoCusto)
                .divide(somatorioCustosQuantidades, SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calcula custo total base
     * FÃ³rmula: PreÃ§o de Compra + Custo Fixo por Unidade
     */
    public static BigDecimal calcularCustoTotalBase(
            BigDecimal precoCusto,
            BigDecimal custoFixoPorUnidade) {

        return precoCusto.add(custoFixoPorUnidade).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calcula lucro bruto por unidade
     * FÃ³rmula: PreÃ§o Venda Ideal Ã— Margem Desejada (%)
     */
    public static BigDecimal calcularLucroBrutoPorUnidade(
            BigDecimal precoVendaIdeal,
            BigDecimal margemDesejada) {

        return precoVendaIdeal
                .multiply(margemDesejada)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }
}
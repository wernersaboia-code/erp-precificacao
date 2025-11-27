package com.erp.precificacao.dto;

import java.math.BigDecimal;

public class ResumoDTO {

    private BigDecimal custoFixoMensal;
    private BigDecimal receitaTotalMensal;
    private BigDecimal custoTotalCompra;
    private BigDecimal margemContribuicaoTotal;
    private Integer vendaEstimada;
    private BigDecimal margemMedia;
    private BigDecimal lucrobrutoTotal;
    private BigDecimal pontoEquilibrio;
    private BigDecimal roiGeral;
    private String status;

    // Getters e Setters
    public BigDecimal getCustoFixoMensal() {
        return custoFixoMensal;
    }

    public void setCustoFixoMensal(BigDecimal custoFixoMensal) {
        this.custoFixoMensal = custoFixoMensal;
    }

    public BigDecimal getReceitaTotalMensal() {
        return receitaTotalMensal;
    }

    public void setReceitaTotalMensal(BigDecimal receitaTotalMensal) {
        this.receitaTotalMensal = receitaTotalMensal;
    }

    public BigDecimal getCustoTotalCompra() {
        return custoTotalCompra;
    }

    public void setCustoTotalCompra(BigDecimal custoTotalCompra) {
        this.custoTotalCompra = custoTotalCompra;
    }

    public BigDecimal getMargemContribuicaoTotal() {
        return margemContribuicaoTotal;
    }

    public void setMargemContribuicaoTotal(BigDecimal margemContribuicaoTotal) {
        this.margemContribuicaoTotal = margemContribuicaoTotal;
    }

    public Integer getVendaEstimada() {
        return vendaEstimada;
    }

    public void setVendaEstimada(Integer vendaEstimada) {
        this.vendaEstimada = vendaEstimada;
    }

    public BigDecimal getMargemMedia() {
        return margemMedia;
    }

    public void setMargemMedia(BigDecimal margemMedia) {
        this.margemMedia = margemMedia;
    }

    public BigDecimal getLucrobrutoTotal() {
        return lucrobrutoTotal;
    }

    public void setLucrobrutoTotal(BigDecimal lucrobrutoTotal) {
        this.lucrobrutoTotal = lucrobrutoTotal;
    }

    public BigDecimal getPontoEquilibrio() {
        return pontoEquilibrio;
    }

    public void setPontoEquilibrio(BigDecimal pontoEquilibrio) {
        this.pontoEquilibrio = pontoEquilibrio;
    }

    public BigDecimal getRoiGeral() {
        return roiGeral;
    }

    public void setRoiGeral(BigDecimal roiGeral) {
        this.roiGeral = roiGeral;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
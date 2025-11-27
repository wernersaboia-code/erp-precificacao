package com.erp.precificacao.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private BigDecimal precoCusto;

    @Column(nullable = false)
    private Integer quantidadeEstimada;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private BigDecimal margemDesejada;

    @Column(nullable = false)
    private BigDecimal impostosCustosVariaveis;

    @Column(nullable = false)
    private BigDecimal custoFixoMensal;

    @Column
    private BigDecimal custoFixoPorUnidade;

    @Column
    private BigDecimal custoTotalBase;

    @Column
    private BigDecimal lucroBrutoPorUnidade;

    @Column
    private BigDecimal precoIdeal;

    @Column
    private BigDecimal margemBruta;

    @Column
    private BigDecimal pontoEquilibrio;

    @Column
    private BigDecimal lucroMensal;

    @Column
    private BigDecimal receita;
}
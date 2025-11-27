package com.erp.precificacao.controller;

import com.erp.precificacao.model.Produto;
import com.erp.precificacao.repository.ProdutoRepository;
import com.erp.precificacao.service.relatorio.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/relatorio")
@CrossOrigin(origins = "*")
public class RelatorioController {

    @Autowired
    private ProdutoRepository produtoRepository;

    /**
     * GET /api/relatorio/csv - Baixa relatório em CSV
     */
    @GetMapping("/csv")
    public ResponseEntity<?> gerarCSV() {
        try {
            List<Produto> produtos = produtoRepository.findAll();

            if (produtos.isEmpty()) {
                return ResponseEntity.badRequest().body("Nenhum produto para gerar relatório");
            }

            String csv = RelatorioService.gerarCSV(produtos);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csv);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erro ao gerar CSV: " + e.getMessage());
        }
    }

    /**
     * GET /api/relatorio/txt - Retorna relatório em TXT
     */
    @GetMapping("/txt")
    public ResponseEntity<?> gerarTXT() {
        try {
            List<Produto> produtos = produtoRepository.findAll();

            if (produtos.isEmpty()) {
                return ResponseEntity.badRequest().body("Nenhum produto para gerar relatório");
            }

            String txt = RelatorioService.gerarTXT(produtos);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio.txt")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(txt);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao gerar TXT: " + e.getMessage());
        }
    }
}

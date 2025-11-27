// 📁 Arquivo: src/main/java/com/projeto/erp/precificacao/controller/ProdutoControllerRefatorado.java
// ℹ️ Este é um exemplo melhorado do controlador de produtos com melhorias e padrões profissionais

package com.erp.precificacao.controller;

import com.erp.precificacao.model.Produto;
import com.erp.precificacao.repository.ProdutoRepository;
import com.erp.precificacao.service.PrecificacaoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller para Produtos - Gestão de produtos do ERP
 *
 * Endpoints:
 * - GET    /api/produtos           - Listar todos os produtos
 * - GET    /api/produtos/{id}      - Obter produto específico
 * - POST   /api/produtos           - Criar novo produto
 * - PUT    /api/produtos/{id}      - Atualizar produto
 * - DELETE /api/produtos/{id}      - Deletar produto
 * - GET    /api/produtos/resumo    - Resumo consolidado
 */
@Slf4j
@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    @Autowired
    private ProdutoRepository produtoRepository;

    /**
     * GET /api/produtos - Listar todos os produtos
     * @return Lista de produtos
     */
    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        log.info("Iniciando listagem de todos os produtos");
        List<Produto> produtos = produtoRepository.findAll();
        log.info("Total de produtos encontrados: {}", produtos.size());
        return ResponseEntity.ok(produtos);
    }

    /**
     * GET /api/produtos/{id} - Obter produto específico
     * @param id ID do produto
     * @return Produto ou 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Produto> obterPorId(@PathVariable Long id) {
        log.info("Buscando produto com ID: {}", id);

        return produtoRepository.findById(id)
                .map(produto -> {
                    log.info("Produto encontrado: {} ({})", produto.getNome(), id);
                    return ResponseEntity.ok(produto);
                })
                .orElseGet(() -> {
                    log.warn("Produto não encontrado com ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * POST /api/produtos - Criar novo produto
     * Calcula automaticamente:
     * - Custo fixo por unidade
     * - Custo total base
     * - Preço ideal
     * - Lucro bruto
     * - Margem bruta
     * - Lucro mensal
     * - Receita
     *
     * @param produto Dados do produto
     * @param bindingResult Validações
     * @return Produto criado com 201 ou erro 400
     */
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody Produto produto, BindingResult bindingResult) {
        log.info("Iniciando criação de novo produto: {}", produto.getNome());

        // Validação de erros do framework
        if (bindingResult.hasErrors()) {
            Map<String, String> erros = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    erros.put(error.getField(), error.getDefaultMessage())
            );
            log.warn("Erros de validação: {}", erros);
            return ResponseEntity.badRequest().body(erros);
        }

        try {
            // Validações de negócio
            if (produto.getMargemDesejada().add(produto.getImpostosCustosVariaveis())
                    .compareTo(BigDecimal.ONE) >= 0) {
                String mensagem = "Margem + Impostos não podem ser >= 100%";
                log.warn(mensagem);
                return ResponseEntity.badRequest().body(Map.of("erro", mensagem));
            }

            // Calcular somatório de (Preço Compra * Quantidade) para todos os produtos
            List<Produto> todosProdutos = produtoRepository.findAll();
            BigDecimal somatorioCustosQuantidades = BigDecimal.ZERO;

            for (Produto p : todosProdutos) {
                BigDecimal custoTotal = p.getPrecoCusto()
                        .multiply(new BigDecimal(p.getQuantidadeEstimada()));
                somatorioCustosQuantidades = somatorioCustosQuantidades.add(custoTotal);
            }

            // Adiciona o novo produto ao somatório
            BigDecimal custoProdutoNovo = produto.getPrecoCusto()
                    .multiply(new BigDecimal(produto.getQuantidadeEstimada()));
            somatorioCustosQuantidades = somatorioCustosQuantidades.add(custoProdutoNovo);

            // Aplicar cálculos de precificação
            aplicarCalculosPrecificacao(produto, somatorioCustosQuantidades);

            // Salvar no banco
            Produto produtoSalvo = produtoRepository.save(produto);

            // Recalcular todos os outros produtos
            recalcularTodosProdutos();

            log.info("Produto criado com sucesso: {} (ID: {})", produtoSalvo.getNome(), produtoSalvo.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(produtoSalvo);

        } catch (IllegalArgumentException e) {
            log.error("Erro de validação ao criar produto: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao criar produto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao criar produto"));
        }
    }

    /**
     * PUT /api/produtos/{id} - Atualizar produto
     * @param id ID do produto
     * @param produtoAtualizado Dados atualizados
     * @param bindingResult Validações
     * @return Produto atualizado ou erro
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @Valid @RequestBody Produto produtoAtualizado,
                                       BindingResult bindingResult) {
        log.info("Iniciando atualização do produto: {}", id);

        if (bindingResult.hasErrors()) {
            Map<String, String> erros = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    erros.put(error.getField(), error.getDefaultMessage())
            );
            log.warn("Erros de validação na atualização: {}", erros);
            return ResponseEntity.badRequest().body(erros);
        }

        Optional<Produto> produtoOpt = produtoRepository.findById(id);

        if (produtoOpt.isEmpty()) {
            log.warn("Produto não encontrado para atualização: {}", id);
            return ResponseEntity.notFound().build();
        }

        try {
            Produto produto = produtoOpt.get();

            // Atualizar campos
            produto.setNome(produtoAtualizado.getNome());
            produto.setPrecoCusto(produtoAtualizado.getPrecoCusto());
            produto.setQuantidadeEstimada(produtoAtualizado.getQuantidadeEstimada());
            produto.setCategoria(produtoAtualizado.getCategoria());
            produto.setMargemDesejada(produtoAtualizado.getMargemDesejada());
            produto.setImpostosCustosVariaveis(produtoAtualizado.getImpostosCustosVariaveis());
            produto.setCustoFixoMensal(produtoAtualizado.getCustoFixoMensal());

            // Recalcular precificação
            recalcularTodosProdutos();

            Produto produtoSalvo = produtoRepository.findById(id).get();
            log.info("Produto atualizado com sucesso: {} (ID: {})", produtoSalvo.getNome(), id);
            return ResponseEntity.ok(produtoSalvo);

        } catch (Exception e) {
            log.error("Erro ao atualizar produto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao atualizar produto"));
        }
    }

    /**
     * DELETE /api/produtos/{id} - Deletar produto
     * @param id ID do produto
     * @return 204 No Content ou 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        log.info("Iniciando deleção do produto: {}", id);

        if (!produtoRepository.existsById(id)) {
            log.warn("Produto não encontrado para deleção: {}", id);
            return ResponseEntity.notFound().build();
        }

        try {
            produtoRepository.deleteById(id);
            recalcularTodosProdutos();
            log.info("Produto deletado com sucesso: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erro ao deletar produto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao deletar produto"));
        }
    }

    /**
     * GET /api/produtos/resumo/consolidado - Resumo consolidado
     * @return Mapa com resumo financeiro
     */
    @GetMapping("/resumo/consolidado")
    public ResponseEntity<?> obterResumo() {
        log.info("Gerando resumo consolidado de produtos");

        try {
            List<Produto> produtos = produtoRepository.findAll();
            Map<String, Object> resultado = new HashMap<>();

            if (produtos.isEmpty()) {
                log.warn("Nenhum produto cadastrado");
                resultado.put("status", "Nenhum produto cadastrado");
                return ResponseEntity.ok(resultado);
            }

            // Cálculos consolidados
            BigDecimal custoFixoMensal = produtos.get(0).getCustoFixoMensal();
            BigDecimal receitaTotalMensal = BigDecimal.ZERO;
            BigDecimal custoTotalCompra = BigDecimal.ZERO;
            BigDecimal lucroTotalPorUnidade = BigDecimal.ZERO;
            Integer vendaEstimada = 0;

            for (Produto p : produtos) {
                if (p.getReceita() != null) {
                    receitaTotalMensal = receitaTotalMensal.add(p.getReceita());
                }

                BigDecimal custoProduto = p.getPrecoCusto()
                        .multiply(new BigDecimal(p.getQuantidadeEstimada()));
                custoTotalCompra = custoTotalCompra.add(custoProduto);

                if (p.getLucroBrutoPorUnidade() != null) {
                    BigDecimal lucroBrutoTotalProduto = p.getLucroBrutoPorUnidade()
                            .multiply(new BigDecimal(p.getQuantidadeEstimada()));
                    lucroTotalPorUnidade = lucroTotalPorUnidade.add(lucroBrutoTotalProduto);
                }

                vendaEstimada += p.getQuantidadeEstimada();
            }

            // Cálculo do ROI Geral
            BigDecimal roiGeral = custoTotalCompra.compareTo(BigDecimal.ZERO) > 0
                    ? lucroTotalPorUnidade.divide(custoTotalCompra, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100))
                    : BigDecimal.ZERO;

            // Determinar status
            String status = receitaTotalMensal.compareTo(custoFixoMensal) >= 0
                    ? "✅ Acima do ponto de equilíbrio!"
                    : "⚠️ Abaixo do ponto de equilíbrio!";

            resultado.put("custoFixoMensal", custoFixoMensal);
            resultado.put("receitaTotalMensal", receitaTotalMensal);
            resultado.put("custoTotalCompra", custoTotalCompra);
            resultado.put("margemContribuicaoTotal", lucroTotalPorUnidade);
            resultado.put("vendaEstimada", vendaEstimada);
            resultado.put("lucrobrutoTotal", lucroTotalPorUnidade);
            resultado.put("roiGeral", roiGeral);
            resultado.put("status", status);

            log.info("Resumo consolidado gerado com sucesso");
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            log.error("Erro ao calcular resumo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao calcular resumo"));
        }
    }

    // ============================================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ============================================================

    /**
     * Aplica os cálculos de precificação a um produto
     */
    private void aplicarCalculosPrecificacao(Produto produto, BigDecimal somatorioCustosQuantidades) {
        // Custo fixo por unidade
        var custoFixoPorUnidade = PrecificacaoService.calcularCustoFixoPorUnidade(
                produto.getCustoFixoMensal(),
                produto.getPrecoCusto(),
                somatorioCustosQuantidades
        );
        produto.setCustoFixoPorUnidade(custoFixoPorUnidade);

        // Custo total base
        var custoTotalBase = PrecificacaoService.calcularCustoTotalBase(
                produto.getPrecoCusto(),
                custoFixoPorUnidade
        );
        produto.setCustoTotalBase(custoTotalBase);

        // Preço ideal
        var precoIdeal = PrecificacaoService.calcularPrecoIdeal(
                custoTotalBase,
                produto.getMargemDesejada(),
                produto.getImpostosCustosVariaveis()
        );
        produto.setPrecoIdeal(precoIdeal);

        // Lucro bruto por unidade
        var lucroBrutoPorUnidade = PrecificacaoService.calcularLucroBrutoPorUnidade(
                precoIdeal,
                produto.getMargemDesejada()
        );
        produto.setLucroBrutoPorUnidade(lucroBrutoPorUnidade);

        // Margem bruta
        var margemBruta = PrecificacaoService.calcularMargemBruta(
                precoIdeal,
                produto.getPrecoCusto()
        );
        produto.setMargemBruta(margemBruta);

        // Lucro mensal
        var lucroMensal = PrecificacaoService.calcularLucroMensal(
                margemBruta,
                produto.getQuantidadeEstimada()
        );
        produto.setLucroMensal(lucroMensal);

        // Receita
        var receita = PrecificacaoService.calcularReceita(
                precoIdeal,
                produto.getQuantidadeEstimada()
        );
        produto.setReceita(receita);
    }

    /**
     * Recalcula todos os produtos quando há mudanças
     */
    private void recalcularTodosProdutos() {
        log.info("Iniciando recálculo de todos os produtos");
        List<Produto> produtos = produtoRepository.findAll();

        if (produtos.isEmpty()) {
            log.info("Nenhum produto para recalcular");
            return;
        }

        // Calcula somatório de (Preço Compra * Quantidade)
        BigDecimal somatorioCustosQuantidades = BigDecimal.ZERO;
        for (Produto p : produtos) {
            BigDecimal custoTotal = p.getPrecoCusto()
                    .multiply(new BigDecimal(p.getQuantidadeEstimada()));
            somatorioCustosQuantidades = somatorioCustosQuantidades.add(custoTotal);
        }

        // Recalcula cada produto
        for (Produto p : produtos) {
            aplicarCalculosPrecificacao(p, somatorioCustosQuantidades);
            produtoRepository.save(p);
        }

        log.info("Recálculo de {} produtos concluído", produtos.size());
    }
}

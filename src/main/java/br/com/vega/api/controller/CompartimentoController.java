package br.com.vega.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.vega.api.dto.CompartimentoDTO;
import br.com.vega.domain.NomeTipoIngrediente;
import br.com.vega.service.CompartimentoService;

@RestController
@RequestMapping("/compartimentos")
public class CompartimentoController {

    private final CompartimentoService compartimentoService;

    public CompartimentoController(CompartimentoService compartimentoService) {
        this.compartimentoService = compartimentoService;
    }

    @GetMapping("/disponiveis-para-venda")
    public ResponseEntity<List<CompartimentoDTO>> listarDisponiveisParaVenda(
            @RequestParam("tipo") NomeTipoIngrediente tipo) {
        return ResponseEntity.ok(compartimentoService.listarDisponiveisParaVenda(tipo));
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<CompartimentoDTO>> listarDisponiveisParaArmazenamento(
            @RequestParam("quantidade") Double quantidade,
            @RequestParam("tipo") NomeTipoIngrediente tipo) {
        return ResponseEntity.ok(compartimentoService.listarDisponiveisParaArmazenamento(quantidade, tipo));
    }
}

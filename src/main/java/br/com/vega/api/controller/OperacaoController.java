package br.com.vega.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.vega.api.dto.OperacaoDTO;
import br.com.vega.service.OperacaoService;

@RestController
public class OperacaoController {

    private final OperacaoService operacaoService;

    public OperacaoController(OperacaoService operacaoService) {
        this.operacaoService = operacaoService;
    }

    @GetMapping("/historico")
    public ResponseEntity<List<OperacaoDTO>> listarHistorico(
            @RequestParam("sortBy") String sortBy,
            @RequestParam("order") String order) {
        return ResponseEntity.ok(operacaoService.listarHistorico(sortBy, order));
    }
}

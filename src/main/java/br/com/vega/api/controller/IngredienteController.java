package br.com.vega.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.vega.api.dto.EntradaIngrediente;
import br.com.vega.api.dto.EntradaIngredienteResponse;
import br.com.vega.api.dto.IngredienteDTO;
import br.com.vega.api.dto.SaidaIngrediente;
import br.com.vega.api.dto.SaidaIngredienteResponse;
import br.com.vega.api.dto.VolumeDisponivelDTO;
import br.com.vega.service.IngredienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/ingredientes")
public class IngredienteController {

    private final IngredienteService ingredienteService;

    public IngredienteController(IngredienteService ingredienteService) {
        this.ingredienteService = ingredienteService;
    }

    @GetMapping
    public ResponseEntity<List<IngredienteDTO>> listarAtivos() {
        return ResponseEntity.ok(ingredienteService.listarAtivos());
    }

    @GetMapping("/volume")
    public ResponseEntity<List<VolumeDisponivelDTO>> listarVolumeDisponivel() {
        return ResponseEntity.ok(ingredienteService.listarVolumeDisponivel());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredienteDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ingredienteService.buscarPorId(id));
    }

    @PostMapping("/entrada")
    public ResponseEntity<EntradaIngredienteResponse> entradaIngrediente(@Valid @RequestBody EntradaIngrediente entrada) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredienteService.entradaIngrediente(entrada));
    }

    @PostMapping("/saida")
    public ResponseEntity<SaidaIngredienteResponse> saidaIngrediente(@Valid @RequestBody SaidaIngrediente saida) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredienteService.saidaIngrediente(saida));
    }
}

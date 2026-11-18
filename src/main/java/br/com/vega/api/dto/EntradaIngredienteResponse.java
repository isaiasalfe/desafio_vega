package br.com.vega.api.dto;

public record EntradaIngredienteResponse(
        Long idIngrediente,
        Long idOperacao,
        Long idCompartimento,
        Double quantidadeArmazenada,
        Double quantidadeDisponivel,
        Double capacidadeAtual) {
}

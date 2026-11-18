package br.com.vega.api.dto;

public record SaidaIngredienteResponse(
        Long idIngrediente,
        Long idOperacao,
        Long idCompartimento,
        Double quantidadeDisponivelIngrediente,
        Boolean flAtivo,
        Double quantidadeArmazenada,
        Double quantidadeDisponivel,
        Double capacidadeAtual) {
}

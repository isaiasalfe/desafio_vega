package br.com.vega.api.dto;

import java.time.LocalDateTime;

public record IngredienteDTO(
        String nome,
        String tipoIngrediente,
        Double quantidadeEntrada,
        Double quantidadeDisponivel,
        String compartimento,
        String responsavel,
        LocalDateTime dtCadastro) {
}

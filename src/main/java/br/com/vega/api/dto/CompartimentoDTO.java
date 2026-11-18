package br.com.vega.api.dto;

import java.time.LocalDateTime;

public record CompartimentoDTO(
        Long id,
        String nome,
        String tipoIngrediente,
        Double quantidadeArmazenada,
        Double quantidadeDisponivel,
        Double capacidadeAtual,
        LocalDateTime dtCadastro,
        LocalDateTime dtUltimoEsvaziamento) {
}

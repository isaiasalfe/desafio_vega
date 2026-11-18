package br.com.vega.api.dto;

import java.time.LocalDateTime;

public record OperacaoDTO(
        Long id,
        LocalDateTime dtCadastro,
        String tipoOperacao,
        Double quantidade,
        String ingrediente,
        String compartimento,
        String responsavel) {
}

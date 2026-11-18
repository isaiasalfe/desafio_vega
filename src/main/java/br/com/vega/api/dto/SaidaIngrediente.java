package br.com.vega.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaidaIngrediente {

    @NotNull(message = "O ingrediente é obrigatório.")
    private Long idIngrediente;

    @NotNull(message = "A quantidade é obrigatória.")
    @Positive(message = "A quantidade deve ser maior que zero.")
    private Double quantidade;

    @NotBlank(message = "O nome do responsável é obrigatório.")
    private String nomeResponsavel;
}

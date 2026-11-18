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
public class EntradaIngrediente {

    @NotBlank(message = "O nome do ingrediente é obrigatório.")
    private String nome;

    @NotNull(message = "O tipo de ingrediente é obrigatório.")
    private Long idTipoIngrediente;

    @NotNull(message = "A quantidade é obrigatória.")
    @Positive(message = "A quantidade deve ser maior que zero.")
    private Double quantidade;

    @NotBlank(message = "O nome do responsável é obrigatório.")
    private String nomeResponsavel;
}

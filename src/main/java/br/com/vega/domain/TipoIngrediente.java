package br.com.vega.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipo_ingrediente")
@Getter
@Setter
@NoArgsConstructor
public class TipoIngrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "dt_cadastro", nullable = false)
    private LocalDateTime dtCadastro;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "nome", nullable = false, length = 20)
    private NomeTipoIngrediente nome;

    @NotNull
    @Column(name = "capacidade", nullable = false)
    private Double capacidade;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "unidade", nullable = false, length = 5)
    private Unidade unidade;
}

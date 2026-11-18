package br.com.vega.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingrediente")
@Getter
@Setter
@NoArgsConstructor
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "dt_cadastro", nullable = false)
    private LocalDateTime dtCadastro;

    @NotNull
    @Column(name = "nome", nullable = false)
    private String nome;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_ingrediente", nullable = false)
    private TipoIngrediente tipoIngrediente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_compartimento", nullable = false)
    private Compartimento compartimento;

    @NotNull
    @Column(name = "quantidade_entrada", nullable = false)
    private Double quantidadeEntrada;

    @NotNull
    @Column(name = "quantidade_disponivel", nullable = false)
    private Double quantidadeDisponivel;

    @NotNull
    @Column(name = "fl_ativo", nullable = false)
    private Boolean flAtivo;
}

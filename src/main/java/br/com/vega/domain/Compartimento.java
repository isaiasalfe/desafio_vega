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
@Table(name = "compartimento")
@Getter
@Setter
@NoArgsConstructor
public class Compartimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "dt_cadastro", nullable = false)
    private LocalDateTime dtCadastro;

    @NotNull
    @Column(name = "nome", nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_ingrediente_atual")
    private TipoIngrediente tipoIngredienteAtual;

    @Column(name = "quantidade_armazenada")
    private Double quantidadeArmazenada;

    @Column(name = "quantidade_disponivel")
    private Double quantidadeDisponivel;

    @Column(name = "capacidade_atual")
    private Double capacidadeAtual;

    @Column(name = "dt_ultimo_esvaziamento")
    private LocalDateTime dtUltimoEsvaziamento;
}

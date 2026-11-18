package br.com.vega.service;

public class IngredienteNaoEncontradoException extends RuntimeException {

    public IngredienteNaoEncontradoException(Long id) {
        super("Ingrediente não encontrado para o id " + id + ".");
    }
}

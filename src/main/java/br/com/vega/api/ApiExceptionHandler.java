package br.com.vega.api;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.vega.service.EntradaInvalidaException;
import br.com.vega.service.IngredienteNaoEncontradoException;
import br.com.vega.service.SaidaInvalidaException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IngredienteNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> ingredienteNaoEncontrado(IngredienteNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler({EntradaInvalidaException.class, SaidaInvalidaException.class})
    public ResponseEntity<Map<String, String>> regraNegocioInvalida(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> dadosInvalidos(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
        if (mensagem.isBlank()) {
            mensagem = "Dados da entrada inválidos.";
        }
        return ResponseEntity.badRequest().body(Map.of("mensagem", mensagem));
    }
}

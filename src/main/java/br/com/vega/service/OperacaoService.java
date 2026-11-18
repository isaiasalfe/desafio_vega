package br.com.vega.service;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.vega.api.dto.OperacaoDTO;
import br.com.vega.domain.Operacao;
import br.com.vega.repository.OperacaoRepository;

@Service
public class OperacaoService {

    private final OperacaoRepository operacaoRepository;

    public OperacaoService(OperacaoRepository operacaoRepository) {
        this.operacaoRepository = operacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<OperacaoDTO> listarHistorico(String sortBy, String order) {
        Sort sort = montarOrdenacao(sortBy, order);
        return operacaoRepository.findAllComRelacionamentos(sort).stream()
                .map(this::toDto)
                .toList();
    }

    private Sort montarOrdenacao(String sortBy, String order) {
        String campo = sortBy == null ? "" : sortBy.trim().toLowerCase(Locale.ROOT);
        String propriedade = switch (campo) {
            case "data" -> "dtCadastro";
            case "compartimento" -> "compartimento.nome";
            default -> throw new EntradaInvalidaException(
                    "Parâmetro sortBy inválido. Use 'data' ou 'compartimento'.");
        };

        String direcao = order == null ? "" : order.trim().toLowerCase(Locale.ROOT);
        Sort.Direction direction = switch (direcao) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new EntradaInvalidaException(
                    "Parâmetro order inválido. Use 'asc' ou 'desc'.");
        };

        return Sort.by(direction, propriedade);
    }

    private OperacaoDTO toDto(Operacao operacao) {
        return new OperacaoDTO(
                operacao.getId(),
                operacao.getDtCadastro(),
                operacao.getTipoOperacao().name(),
                operacao.getQuantidade(),
                operacao.getIngrediente().getNome(),
                operacao.getCompartimento().getNome(),
                operacao.getNomeResponsavel());
    }
}

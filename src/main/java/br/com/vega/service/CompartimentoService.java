package br.com.vega.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.vega.api.dto.CompartimentoDTO;
import br.com.vega.domain.Compartimento;
import br.com.vega.domain.NomeTipoIngrediente;
import br.com.vega.repository.CompartimentoRepository;

@Service
public class CompartimentoService {

    private final CompartimentoRepository compartimentoRepository;
    private final IngredienteService ingredienteService;

    public CompartimentoService(
            CompartimentoRepository compartimentoRepository,
            IngredienteService ingredienteService) {
        this.compartimentoRepository = compartimentoRepository;
        this.ingredienteService = ingredienteService;
    }

    @Transactional(readOnly = true)
    public List<CompartimentoDTO> listarDisponiveisParaVenda(NomeTipoIngrediente tipo) {
        return compartimentoRepository.findDisponiveisParaVendaPorTipo(tipo).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompartimentoDTO> listarDisponiveisParaArmazenamento(Double quantidade, NomeTipoIngrediente tipo) {
        return ingredienteService.listarCompartimentosDisponiveisParaArmazenamento(tipo, quantidade).stream()
                .map(this::toDto)
                .toList();
    }

    private CompartimentoDTO toDto(Compartimento compartimento) {
        return new CompartimentoDTO(
                compartimento.getId(),
                compartimento.getNome(),
                compartimento.getTipoIngredienteAtual() != null
                        ? compartimento.getTipoIngredienteAtual().getNome().name()
                        : null,
                compartimento.getQuantidadeArmazenada(),
                compartimento.getQuantidadeDisponivel(),
                compartimento.getCapacidadeAtual(),
                compartimento.getDtCadastro(),
                compartimento.getDtUltimoEsvaziamento());
    }
}

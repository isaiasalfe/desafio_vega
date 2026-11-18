package br.com.vega.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.vega.api.dto.EntradaIngrediente;
import br.com.vega.api.dto.EntradaIngredienteResponse;
import br.com.vega.api.dto.IngredienteDTO;
import br.com.vega.api.dto.SaidaIngrediente;
import br.com.vega.api.dto.SaidaIngredienteResponse;
import br.com.vega.api.dto.VolumeDisponivelDTO;
import br.com.vega.domain.Compartimento;
import br.com.vega.domain.Ingrediente;
import br.com.vega.domain.NomeTipoIngrediente;
import br.com.vega.domain.Operacao;
import br.com.vega.domain.TipoIngrediente;
import br.com.vega.domain.TipoOperacao;
import br.com.vega.repository.CompartimentoRepository;
import br.com.vega.repository.IngredienteRepository;
import br.com.vega.repository.OperacaoRepository;
import br.com.vega.repository.TipoIngredienteRepository;

@Service
public class IngredienteService {

    private final IngredienteRepository ingredienteRepository;
    private final OperacaoRepository operacaoRepository;
    private final CompartimentoRepository compartimentoRepository;
    private final TipoIngredienteRepository tipoIngredienteRepository;

    public IngredienteService(
            IngredienteRepository ingredienteRepository,
            OperacaoRepository operacaoRepository,
            CompartimentoRepository compartimentoRepository,
            TipoIngredienteRepository tipoIngredienteRepository) {
        this.ingredienteRepository = ingredienteRepository;
        this.operacaoRepository = operacaoRepository;
        this.compartimentoRepository = compartimentoRepository;
        this.tipoIngredienteRepository = tipoIngredienteRepository;
    }

    @Transactional(readOnly = true)
    public List<IngredienteDTO> listarAtivos() {
        List<Ingrediente> ingredientes = ingredienteRepository.findAtivosComRelacionamentos();
        if (ingredientes.isEmpty()) {
            return List.of();
        }

        Map<Long, Operacao> entradasPorIngrediente = operacaoRepository
                .findByIngredienteIdInAndTipoOperacao(
                        ingredientes.stream().map(Ingrediente::getId).toList(),
                        TipoOperacao.ENTRADA)
                .stream()
                .collect(Collectors.toMap(
                        operacao -> operacao.getIngrediente().getId(),
                        Function.identity(),
                        (primeira, ignorada) -> primeira));

        return ingredientes.stream()
                .map(ingrediente -> toDto(ingrediente, entradasPorIngrediente.get(ingrediente.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public IngredienteDTO buscarPorId(Long id) {
        Ingrediente ingrediente = ingredienteRepository.findByIdComRelacionamentos(id)
                .orElseThrow(() -> new IngredienteNaoEncontradoException(id));
        Operacao entrada = operacaoRepository
                .findFirstByIngredienteIdAndTipoOperacaoOrderByIdAsc(id, TipoOperacao.ENTRADA)
                .orElse(null);
        return toDto(ingrediente, entrada);
    }

    @Transactional(readOnly = true)
    public List<VolumeDisponivelDTO> listarVolumeDisponivel() {
        return tipoIngredienteRepository.somarQuantidadeArmazenadaPorTipo().stream()
                .map(linha -> new VolumeDisponivelDTO(
                        ((NomeTipoIngrediente) linha[0]).name(),
                        ((Number) linha[1]).doubleValue()))
                .toList();
    }

    @Transactional
    public EntradaIngredienteResponse entradaIngrediente(EntradaIngrediente entrada) {
        TipoIngrediente tipo = tipoIngredienteRepository.findById(entrada.getIdTipoIngrediente())
                .orElseThrow(() -> new EntradaInvalidaException("Tipo de ingrediente não encontrado."));

        Compartimento compartimento = buscarCompartimentoIdeal(entrada, tipo);

        LocalDateTime agora = LocalDateTime.now();
        Ingrediente ingrediente = ingredienteRepository.save(novoIngrediente(entrada, tipo, compartimento, agora));
        Operacao operacao = operacaoRepository.save(novaOperacaoEntrada(entrada, ingrediente, compartimento, agora));
        atualizarCompartimentoEntrada(compartimento, tipo, entrada.getQuantidade());

        return new EntradaIngredienteResponse(
                ingrediente.getId(),
                operacao.getId(),
                compartimento.getId(),
                compartimento.getQuantidadeArmazenada(),
                compartimento.getQuantidadeDisponivel(),
                compartimento.getCapacidadeAtual());
    }

    @Transactional
    public SaidaIngredienteResponse saidaIngrediente(SaidaIngrediente saida) {
        Ingrediente ingrediente = ingredienteRepository.findByIdComRelacionamentos(saida.getIdIngrediente())
                .orElseThrow(() -> new SaidaInvalidaException("Ingrediente não encontrado."));

        validarSaida(saida, ingrediente);

        LocalDateTime agora = LocalDateTime.now();
        Compartimento compartimento = ingrediente.getCompartimento();
        Operacao operacao = operacaoRepository.save(novaOperacaoSaida(saida, ingrediente, compartimento, agora));
        atualizarIngredienteSaida(ingrediente, saida.getQuantidade());
        atualizarCompartimentoSaida(compartimento, saida.getQuantidade(), agora);

        return new SaidaIngredienteResponse(
                ingrediente.getId(),
                operacao.getId(),
                compartimento.getId(),
                ingrediente.getQuantidadeDisponivel(),
                ingrediente.getFlAtivo(),
                compartimento.getQuantidadeArmazenada(),
                compartimento.getQuantidadeDisponivel(),
                compartimento.getCapacidadeAtual());
    }

    private void validarSaida(SaidaIngrediente saida, Ingrediente ingrediente) {
        if (!Boolean.TRUE.equals(ingrediente.getFlAtivo())) {
            throw new SaidaInvalidaException("O ingrediente informado não está ativo.");
        }

        double disponivel = valor(ingrediente.getQuantidadeDisponivel());
        if (disponivel < saida.getQuantidade()) {
            throw new SaidaInvalidaException(
                    "Quantidade disponível insuficiente no ingrediente. Disponível: "
                            + disponivel
                            + ", solicitado: "
                            + saida.getQuantidade()
                            + ".");
        }
    }

    private void atualizarIngredienteSaida(Ingrediente ingrediente, double quantidade) {
        double novaDisponivel = valor(ingrediente.getQuantidadeDisponivel()) - quantidade;
        ingrediente.setQuantidadeDisponivel(novaDisponivel);
        if (novaDisponivel == 0d) {
            ingrediente.setFlAtivo(false);
        }
        ingredienteRepository.save(ingrediente);
    }

    private void atualizarCompartimentoSaida(Compartimento compartimento, double quantidade, LocalDateTime agora) {
        double capacidade = compartimento.getCapacidadeAtual() != null
                ? compartimento.getCapacidadeAtual()
                : (compartimento.getTipoIngredienteAtual() != null
                        ? compartimento.getTipoIngredienteAtual().getCapacidade()
                        : valor(compartimento.getQuantidadeArmazenada()) + valor(compartimento.getQuantidadeDisponivel()));
        double armazenada = valor(compartimento.getQuantidadeArmazenada()) - quantidade;
        double disponivel = capacidade - armazenada;

        compartimento.setCapacidadeAtual(capacidade);
        compartimento.setQuantidadeArmazenada(armazenada);
        compartimento.setQuantidadeDisponivel(disponivel);
        if (armazenada == 0d) {
            compartimento.setDtUltimoEsvaziamento(agora);
        }
        compartimentoRepository.save(compartimento);
    }

    private IngredienteDTO toDto(Ingrediente ingrediente, Operacao entrada) {
        return new IngredienteDTO(
                ingrediente.getNome(),
                ingrediente.getTipoIngrediente().getNome().name(),
                ingrediente.getQuantidadeEntrada(),
                ingrediente.getQuantidadeDisponivel(),
                ingrediente.getCompartimento().getNome(),
                entrada != null ? entrada.getNomeResponsavel() : null,
                ingrediente.getDtCadastro());
    }

    @Transactional(readOnly = true)
    public List<Compartimento> listarCompartimentosDisponiveisParaArmazenamento(
            NomeTipoIngrediente nomeTipo,
            Double quantidade) {
        TipoIngrediente tipo = tipoIngredienteRepository.findByNome(nomeTipo)
                .orElseThrow(() -> new EntradaInvalidaException("Tipo de ingrediente não encontrado."));

        List<Compartimento> disponiveis = new ArrayList<>();
        for (Compartimento compartimento : compartimentoRepository.findDisponiveisParaEntrada()) {
            try {
                validarEntrada(quantidade, tipo, compartimento);
                disponiveis.add(compartimento);
            } catch (EntradaInvalidaException ignored) {
                // segue para o próximo candidato
            }
        }
        return disponiveis;
    }

    private Compartimento buscarCompartimentoIdeal(EntradaIngrediente entrada, TipoIngrediente tipo) {
        List<Compartimento> candidatos = compartimentoRepository.findDisponiveisParaEntrada();

        if (candidatos.isEmpty()) {
            throw new EntradaInvalidaException(
                    "Nenhum compartimento disponível para a entrada foi encontrado.");
        }

        EntradaInvalidaException ultimaFalha = null;
        for (Compartimento compartimento : candidatos) {
            try {
                validarEntrada(entrada.getQuantidade(), tipo, compartimento);
                return compartimento;
            } catch (EntradaInvalidaException ex) {
                ultimaFalha = ex;
            }
        }

        throw ultimaFalha != null
                ? ultimaFalha
                : new EntradaInvalidaException("Nenhum compartimento válido para a entrada.");
    }

    private void validarEntrada(Double quantidade, TipoIngrediente tipo, Compartimento compartimento) {
        validarTipoDoCompartimento(tipo, compartimento);

        double disponivel = quantidadeDisponivel(tipo, compartimento);
        if (disponivel < quantidade) {
            throw new EntradaInvalidaException(
                    "Quantidade disponível insuficiente no compartimento para o tipo "
                            + tipo.getNome()
                            + ". Disponível: "
                            + disponivel
                            + ", solicitado: "
                            + quantidade
                            + ".");
        }
    }

    private void validarTipoDoCompartimento(TipoIngrediente tipo, Compartimento compartimento) {
        if (mesmoTipo(tipo, compartimento) || compartimento.getTipoIngredienteAtual() == null) {
            return;
        }
        if (!compartimentoLivre(compartimento)) {
            throw new EntradaInvalidaException(
                    "Não é possível entrar um tipo de ingrediente diferente do tipo atual do compartimento enquanto a quantidade armazenada não for zero.");
        }
        if (!diaSeguinteAoUltimoEsvaziamento(compartimento.getDtUltimoEsvaziamento())) {
            throw new EntradaInvalidaException(
                    "A entrada de um tipo de ingrediente diferente só é permitida no dia seguinte à data do último esvaziamento, com o compartimento totalmente livre.");
        }
    }

    private void atualizarCompartimentoEntrada(Compartimento compartimento, TipoIngrediente tipo, double quantidade) {
        double disponivel = quantidadeDisponivel(tipo, compartimento);
        double capacidade = capacidadeAtualizada(compartimento, tipo);
        double armazenada = valor(compartimento.getQuantidadeArmazenada()) + quantidade;

        compartimento.setTipoIngredienteAtual(tipo);
        compartimento.setQuantidadeArmazenada(armazenada);
        compartimento.setQuantidadeDisponivel(disponivel - quantidade);
        compartimento.setCapacidadeAtual(capacidade);
        compartimentoRepository.save(compartimento);
    }

    private double quantidadeDisponivel(TipoIngrediente tipo, Compartimento compartimento) {
        if (mesmoTipo(tipo, compartimento) && compartimento.getQuantidadeDisponivel() != null) {
            return compartimento.getQuantidadeDisponivel();
        }
        if (!mesmoTipo(tipo, compartimento)) {
            return tipo.getCapacidade();
        }
        double capacidade = compartimento.getCapacidadeAtual() != null
                ? compartimento.getCapacidadeAtual()
                : tipo.getCapacidade();
        return capacidade - valor(compartimento.getQuantidadeArmazenada());
    }

    private double capacidadeAtualizada(Compartimento compartimento, TipoIngrediente tipo) {
        if (!mesmoTipo(tipo, compartimento) || compartimento.getCapacidadeAtual() == null) {
            return tipo.getCapacidade();
        }
        return compartimento.getCapacidadeAtual();
    }

    private boolean mesmoTipo(TipoIngrediente tipo, Compartimento compartimento) {
        TipoIngrediente tipoAtual = compartimento.getTipoIngredienteAtual();
        return tipoAtual != null && Objects.equals(tipoAtual.getId(), tipo.getId());
    }

    private boolean compartimentoLivre(Compartimento compartimento) {
        Double armazenada = compartimento.getQuantidadeArmazenada();
        return armazenada == null || armazenada == 0d;
    }

    private boolean diaSeguinteAoUltimoEsvaziamento(LocalDateTime dtUltimoEsvaziamento) {
        if (dtUltimoEsvaziamento == null) {
            return false;
        }
        return LocalDate.now().equals(dtUltimoEsvaziamento.toLocalDate().plusDays(1));
    }

    private double valor(Double valor) {
        return valor == null ? 0d : valor;
    }

    private Ingrediente novoIngrediente(
            EntradaIngrediente entrada,
            TipoIngrediente tipo,
            Compartimento compartimento,
            LocalDateTime agora) {
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setDtCadastro(agora);
        ingrediente.setNome(entrada.getNome());
        ingrediente.setTipoIngrediente(tipo);
        ingrediente.setCompartimento(compartimento);
        ingrediente.setQuantidadeEntrada(entrada.getQuantidade());
        ingrediente.setQuantidadeDisponivel(entrada.getQuantidade());
        ingrediente.setFlAtivo(true);
        return ingrediente;
    }

    private Operacao novaOperacaoEntrada(
            EntradaIngrediente entrada,
            Ingrediente ingrediente,
            Compartimento compartimento,
            LocalDateTime agora) {
        Operacao operacao = new Operacao();
        operacao.setDtCadastro(agora);
        operacao.setTipoOperacao(TipoOperacao.ENTRADA);
        operacao.setQuantidade(entrada.getQuantidade());
        operacao.setIngrediente(ingrediente);
        operacao.setCompartimento(compartimento);
        operacao.setNomeResponsavel(entrada.getNomeResponsavel());
        return operacao;
    }

    private Operacao novaOperacaoSaida(
            SaidaIngrediente saida,
            Ingrediente ingrediente,
            Compartimento compartimento,
            LocalDateTime agora) {
        Operacao operacao = new Operacao();
        operacao.setDtCadastro(agora);
        operacao.setTipoOperacao(TipoOperacao.SAIDA);
        operacao.setQuantidade(saida.getQuantidade());
        operacao.setIngrediente(ingrediente);
        operacao.setCompartimento(compartimento);
        operacao.setNomeResponsavel(saida.getNomeResponsavel());
        return operacao;
    }
}

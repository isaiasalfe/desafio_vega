package br.com.vega.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import br.com.vega.api.dto.EntradaIngrediente;
import br.com.vega.domain.Compartimento;
import br.com.vega.domain.NomeTipoIngrediente;
import br.com.vega.domain.TipoIngrediente;
import br.com.vega.domain.Unidade;
import br.com.vega.repository.CompartimentoRepository;
import br.com.vega.repository.TipoIngredienteRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IngredienteControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TipoIngredienteRepository tipoIngredienteRepository;

    @Autowired
    private CompartimentoRepository compartimentoRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void limparBase() {
        jdbcTemplate.update("delete from operacao");
        jdbcTemplate.update("delete from ingrediente");
        jdbcTemplate.update("delete from compartimento");
        jdbcTemplate.update("delete from tipo_ingrediente");
    }

    @Test
    void entradaNoMesmoTipoGravaIngredienteOperacaoEAtualizaCompartimento() throws Exception {
        TipoIngrediente seco = tipo(NomeTipoIngrediente.SECO, 100d, Unidade.KG);
        Compartimento silo = compartimento("Silo 1", seco, 20d, 80d, 100d, null);

        HttpResponse<String> response = post(new EntradaIngrediente("Acucar", seco.getId(), 30d, "Maria"));

        assertThat(response.statusCode()).isEqualTo(201);
        JsonNode body = objectMapper.readTree(response.body());
        assertThat(body.get("idCompartimento").asLong()).isEqualTo(silo.getId());
        assertThat(body.get("quantidadeArmazenada").asDouble()).isEqualTo(50d);
        assertThat(body.get("quantidadeDisponivel").asDouble()).isEqualTo(50d);
        assertThat(body.get("capacidadeAtual").asDouble()).isEqualTo(100d);

        Long idIngrediente = body.get("idIngrediente").asLong();
        Long idOperacao = body.get("idOperacao").asLong();

        assertThat(jdbcTemplate.queryForObject(
                "select nome from ingrediente where id = ?", String.class, idIngrediente)).isEqualTo("Acucar");
        assertThat(jdbcTemplate.queryForObject(
                "select quantidade_disponivel from ingrediente where id = ?", Double.class, idIngrediente)).isEqualTo(30d);
        assertThat(jdbcTemplate.queryForObject(
                "select fl_ativo from ingrediente where id = ?", Boolean.class, idIngrediente)).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "select tipo_operacao from operacao where id = ?", String.class, idOperacao)).isEqualTo("ENTRADA");
        assertThat(jdbcTemplate.queryForObject(
                "select nome_responsavel from operacao where id = ?", String.class, idOperacao)).isEqualTo("Maria");
        assertThat(jdbcTemplate.queryForObject(
                "select id_ingrediente from operacao where id = ?", Long.class, idOperacao)).isEqualTo(idIngrediente);
        assertThat(jdbcTemplate.queryForObject(
                "select id_compartimento from ingrediente where id = ?", Long.class, idIngrediente)).isEqualTo(silo.getId());
    }

    @Test
    void rejeitaQuandoQuantidadeDisponivelNaoCobreAEntrada() throws Exception {
        TipoIngrediente seco = tipo(NomeTipoIngrediente.SECO, 100d, Unidade.KG);
        Compartimento silo = compartimento("Silo 2", seco, 90d, 10d, 100d, null);

        int ingredientesAntes = contar("ingrediente");
        int operacoesAntes = contar("operacao");

        HttpResponse<String> response = post(new EntradaIngrediente("Farinha", seco.getId(), 15d, "Joao"));

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(objectMapper.readTree(response.body()).get("mensagem").asText())
                .contains("Quantidade disponível insuficiente");
        assertThat(contar("ingrediente")).isEqualTo(ingredientesAntes);
        assertThat(contar("operacao")).isEqualTo(operacoesAntes);
        assertThat(jdbcTemplate.queryForObject(
                "select quantidade_armazenada from compartimento where id = ?", Double.class, silo.getId()))
                .isEqualTo(90d);
    }

    @Test
    void rejeitaTipoDiferenteComQuantidadeArmazenada() throws Exception {
        TipoIngrediente seco = tipo(NomeTipoIngrediente.SECO, 100d, Unidade.KG);
        TipoIngrediente liquido = tipo(NomeTipoIngrediente.LIQUIDO, 80d, Unidade.LT);
        compartimento("Silo 3", seco, 20d, 80d, 100d, LocalDate.now().minusDays(1).atStartOfDay());

        HttpResponse<String> response = post(new EntradaIngrediente("Oleo", liquido.getId(), 10d, "Joao"));

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(objectMapper.readTree(response.body()).get("mensagem").asText())
                .contains("quantidade armazenada não for zero");
    }

    @Test
    void rejeitaTipoDiferenteForaDoDiaSeguinteAoEsvaziamento() throws Exception {
        TipoIngrediente seco = tipo(NomeTipoIngrediente.SECO, 100d, Unidade.KG);
        TipoIngrediente refrigerado = tipo(NomeTipoIngrediente.REFRIGERADO, 40d, Unidade.KG);
        compartimento("Silo 4", seco, 0d, 100d, 100d, LocalDateTime.now());

        HttpResponse<String> response = post(new EntradaIngrediente("Leite", refrigerado.getId(), 5d, "Ana"));

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(objectMapper.readTree(response.body()).get("mensagem").asText())
                .contains("dia seguinte");
    }

    @Test
    void aceitaTipoDiferenteNoDiaSeguinteComCompartimentoLivre() throws Exception {
        TipoIngrediente seco = tipo(NomeTipoIngrediente.SECO, 100d, Unidade.KG);
        TipoIngrediente liquido = tipo(NomeTipoIngrediente.LIQUIDO, 80d, Unidade.LT);
        Compartimento silo = compartimento(
                "Silo 5", seco, 0d, 100d, 100d, LocalDate.now().minusDays(1).atStartOfDay());

        HttpResponse<String> response = post(new EntradaIngrediente("Oleo", liquido.getId(), 20d, "Ana"));

        assertThat(response.statusCode()).isEqualTo(201);
        JsonNode body = objectMapper.readTree(response.body());
        assertThat(body.get("idCompartimento").asLong()).isEqualTo(silo.getId());
        assertThat(body.get("quantidadeArmazenada").asDouble()).isEqualTo(20d);
        assertThat(body.get("quantidadeDisponivel").asDouble()).isEqualTo(60d);
        assertThat(body.get("capacidadeAtual").asDouble()).isEqualTo(80d);
        assertThat(jdbcTemplate.queryForObject(
                "select id_tipo_ingrediente_atual from compartimento where id = ?",
                Long.class,
                silo.getId())).isEqualTo(liquido.getId());
    }

    @Test
    void aceitaPrimeiraEntradaEmCompartimentoSemTipo() throws Exception {
        TipoIngrediente seco = tipo(NomeTipoIngrediente.SECO, 50d, Unidade.KG);
        Compartimento silo = compartimento("Silo 6", null, null, null, null, null);

        HttpResponse<String> response = post(new EntradaIngrediente("Arroz", seco.getId(), 15d, "Ana"));

        assertThat(response.statusCode()).isEqualTo(201);
        JsonNode body = objectMapper.readTree(response.body());
        assertThat(body.get("idCompartimento").asLong()).isEqualTo(silo.getId());
        assertThat(body.get("quantidadeArmazenada").asDouble()).isEqualTo(15d);
        assertThat(body.get("quantidadeDisponivel").asDouble()).isEqualTo(35d);
        assertThat(body.get("capacidadeAtual").asDouble()).isEqualTo(50d);
    }

    @Test
    void rejeitaEntradaSemDadosObrigatorios() throws Exception {
        HttpResponse<String> response = post(new EntradaIngrediente(" ", null, 0d, ""));

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(objectMapper.readTree(response.body()).get("mensagem").asText())
                .contains("obrigatório");
    }

    private HttpResponse<String> post(EntradaIngrediente entrada) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/ingredientes/entrada"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(entrada)))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private TipoIngrediente tipo(NomeTipoIngrediente nome, double capacidade, Unidade unidade) {
        TipoIngrediente tipo = new TipoIngrediente();
        tipo.setDtCadastro(LocalDateTime.now());
        tipo.setNome(nome);
        tipo.setCapacidade(capacidade);
        tipo.setUnidade(unidade);
        return tipoIngredienteRepository.save(tipo);
    }

    private Compartimento compartimento(
            String nome,
            TipoIngrediente tipo,
            Double armazenada,
            Double disponivel,
            Double capacidade,
            LocalDateTime ultimoEsvaziamento) {
        Compartimento compartimento = new Compartimento();
        compartimento.setDtCadastro(LocalDateTime.now());
        compartimento.setNome(nome);
        compartimento.setTipoIngredienteAtual(tipo);
        compartimento.setQuantidadeArmazenada(armazenada);
        compartimento.setQuantidadeDisponivel(disponivel);
        compartimento.setCapacidadeAtual(capacidade);
        compartimento.setDtUltimoEsvaziamento(ultimoEsvaziamento);
        return compartimentoRepository.save(compartimento);
    }

    private int contar(String tabela) {
        Integer total = jdbcTemplate.queryForObject("select count(*) from " + tabela, Integer.class);
        return total == null ? 0 : total;
    }
}

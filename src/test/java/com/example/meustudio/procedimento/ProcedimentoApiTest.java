package com.example.meustudio.procedimento;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Contratos reais com PostgreSQL: cada teste é revertido, sem limpar dados de outras suítes. */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@Transactional
class ProcedimentoApiTest {
    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    MockMvc mvc;

    @BeforeEach
    void setup() { mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); }

    private String dados(String nome) {
        return "{\"nome\":\"" + nome + "\",\"preco\":150.00,\"duracaoMinutos\":45,\"categoria\":\"Sobrancelhas\"}";
    }

    private JsonNode criar(String nome) throws Exception {
        String body = mvc.perform(post("/procedimentos").with(user("teste").roles("USER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(dados(nome)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.ativo").value(true))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    @Test
    void deveCadastrarEditarFiltrarPaginarEDesativarSemPerderCadastro() throws Exception {
        String prefix = UUID.randomUUID().toString();
        String id = criar(prefix + " B").get("id").asString();
        criar(prefix + " A");
        mvc.perform(get("/procedimentos").with(user("teste")).param("nome", prefix).param("ativo", "true").param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2)).andExpect(jsonPath("$.content[0].nome").value(prefix + " A"));
        mvc.perform(get("/procedimentos").with(user("teste")).param("nome", prefix).param("size", "1").param("page", "1"))
                .andExpect(jsonPath("$.content[0].id").value(id));
        mvc.perform(put("/procedimentos/" + id).with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(dados(prefix + " Renomeado"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(prefix + " Renomeado"));
        for (boolean ativo : new boolean[]{false, true}) {
            mvc.perform(put("/procedimentos/" + id + "/status").with(user("teste")).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":" + ativo + "}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(ativo));
            mvc.perform(get("/procedimentos").with(user("teste")).param("nome", prefix + " Renomeado")
                    .param("categoria", "Sobrancelhas").param("ativo", String.valueOf(ativo)))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
        mvc.perform(get("/procedimentos/categorias").with(user("teste")))
                .andExpect(status().isOk()).andExpect(jsonPath("$", org.hamcrest.Matchers.hasItem("Sobrancelhas")));
        mvc.perform(get("/procedimentos").with(user("teste")).param("nome", prefix).param("categoria", "Não existe"))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void deveRecusarNomeRepetidoInclusiveInativoEPermitirEditarProprioNome() throws Exception {
        String nome = "Design " + UUID.randomUUID();
        String id = criar(nome).get("id").asString();
        mvc.perform(put("/procedimentos/" + id).with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(dados(nome)))
                .andExpect(status().isOk());
        mvc.perform(put("/procedimentos/" + id + "/status").with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
                .andExpect(status().isOk());
        mvc.perform(post("/procedimentos").with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(dados("  " + nome.toUpperCase() + "  ")))
                .andExpect(status().isBadRequest());
        String otherId = criar("Outro " + UUID.randomUUID()).get("id").asString();
        mvc.perform(put("/procedimentos/" + otherId).with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(dados(nome)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"nome\":\" \",\"preco\":1,\"duracaoMinutos\":1}",
            "{\"nome\":\"X\",\"preco\":0,\"duracaoMinutos\":1}", "{\"nome\":\"X\",\"preco\":-1,\"duracaoMinutos\":1}",
            "{\"nome\":\"X\",\"preco\":1.001,\"duracaoMinutos\":1}", "{\"nome\":\"X\",\"preco\":10000000000,\"duracaoMinutos\":1}",
            "{\"nome\":\"X\",\"preco\":1,\"duracaoMinutos\":0}", "{\"nome\":\"X\",\"preco\":1,\"duracaoMinutos\":-1}",
            "{\"nome\":\"X\",\"preco\":1,\"duracaoMinutos\":1.5}", "{\"nome\":\"X\",\"preco\":1,\"duracaoMinutos\":2147483648}"})
    void deveRecusarDadosInvalidosSemSalvar(String body) throws Exception {
        mvc.perform(post("/procedimentos").with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRecusarLimitesDeTextoEAceitarOpcionaisVazios() throws Exception {
        for (String campo : new String[]{"nome", "descricao", "categoria"}) {
            var body = mapper.createObjectNode().put("nome", "Nome").put("preco", 1).put("duracaoMinutos", 1);
            body.put(campo, "x".repeat(campo.equals("nome") ? 161 : campo.equals("descricao") ? 2001 : 81));
            mvc.perform(post("/procedimentos").with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/procedimentos").with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"  " + UUID.randomUUID() + "  \",\"preco\":0.01,\"duracaoMinutos\":1,\"descricao\":\" \",\"categoria\":\"\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.categoria").isEmpty()).andExpect(jsonPath("$.descricao").isEmpty());
    }

    @Test
    void deveExigirAutenticacaoCsrfEIdsValidos() throws Exception {
        mvc.perform(get("/procedimentos")).andExpect(status().isForbidden());
        mvc.perform(post("/procedimentos").with(user("teste")).contentType(MediaType.APPLICATION_JSON).content(dados("X")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/procedimentos/" + UUID.randomUUID()).with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(dados("X")))
                .andExpect(status().isNotFound());
        mvc.perform(put("/procedimentos/invalido/status").with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/procedimentos/" + UUID.randomUUID() + "/status").with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        for (String size : new String[]{"0", "51", "x"}) mvc.perform(get("/procedimentos").with(user("teste")).param("size", size)).andExpect(status().isBadRequest());
        mvc.perform(get("/procedimentos").with(user("teste")).param("page", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/procedimentos").with(user("teste").roles("ADMIN"))).andExpect(status().isOk());
    }

    @Test
    void devePreservarNomeEValorHistoricosEExigirProcedimentoAtivo() throws Exception {
        String nome = "Historico " + UUID.randomUUID();
        String id = criar(nome).get("id").asString();
        String body = "{\"data\":\"" + LocalDate.now() + "\",\"cliente\":\"Maria\",\"procedimentoId\":\"" + id + "\",\"valor\":125.50,\"meioDePagamento\":\"PIX\"}";
        var faturamento = mvc.perform(post("/financeiro/lancar").with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.valor").value(125.50)).andExpect(jsonPath("$.procedimento").value(nome))
                .andReturn().getResponse().getContentAsString();
        String fatId = mapper.readTree(faturamento).get("id").asString();
        mvc.perform(put("/procedimentos/" + id).with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(dados("Alterado " + UUID.randomUUID()).replace("150.00", "300.00"))).andExpect(status().isOk());
        mvc.perform(put("/procedimentos/" + id + "/status").with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
                .andExpect(status().isOk());
        assertEquals(nome, jdbc.queryForObject("select procedimento from faturamentos where fatid = ?", String.class, UUID.fromString(fatId)));
        assertEquals(id, jdbc.queryForObject("select procedimento_id::text from faturamentos where fatid = ?", String.class, UUID.fromString(fatId)));
        assertEquals("125.50", jdbc.queryForObject("select valorbrutofaturamento::text from faturamentos where fatid = ?", String.class, UUID.fromString(fatId)));
        mvc.perform(post("/financeiro/lancar").with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        mvc.perform(post("/financeiro/lancar").with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body.replace(id, UUID.randomUUID().toString()))).andExpect(status().isNotFound());
        mvc.perform(post("/financeiro/lancar").with(user("teste")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body.replace("\"procedimentoId\":\"" + id + "\"", "\"procedimento\":\"Texto antigo\""))).andExpect(status().isBadRequest());
    }

    @Test
    void buscaDeveTratarCuringasComoTextoLiteral() throws Exception {
        String prefix = UUID.randomUUID().toString();
        criar(prefix + " %_\\\\");
        criar(prefix + " ABC");
        mvc.perform(get("/procedimentos").with(user("teste")).param("nome", prefix + " %_\\"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }
}

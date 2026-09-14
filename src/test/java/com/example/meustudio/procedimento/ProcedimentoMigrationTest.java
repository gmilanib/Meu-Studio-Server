package com.example.meustudio.procedimento;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class ProcedimentoMigrationTest {
    /** Espera que V8 preserve dados de V7 e que o índice impeça duplicatas mesmo sem passar pelo serviço. */
    @Test
    void deveMigrarHistoricoEImpedirNomeDuplicadoNoBanco() throws Exception {
        String url = System.getenv().getOrDefault("TEST_DATABASE_URL", "jdbc:postgresql://127.0.0.1:55439/meu_studio_procedimentos_test");
        String user = System.getenv().getOrDefault("TEST_DATABASE_USER", "gmb");
        String password = System.getenv().getOrDefault("TEST_DATABASE_PASSWORD", "");
        String schema = "migration_test_" + UUID.randomUUID().toString().replace("-", "");
        try (var connection = DriverManager.getConnection(url, user, password); var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schema);
            try {
                Flyway.configure().dataSource(url, user, password).schemas(schema).defaultSchema(schema).target("7").load().migrate();
                statement.execute("SET search_path TO " + schema);
                statement.execute("INSERT INTO faturamentos (fatid, datafaturamento, clientename, procedimento, valorbrutofaturamento, meiopagamento) VALUES ('00000000-0000-0000-0000-000000000001', CURRENT_DATE, 'Maria', 'Serviço antigo', 120.50, 'PIX')");
                Flyway.configure().dataSource(url, user, password).schemas(schema).defaultSchema(schema).load().migrate();
                try (var result = statement.executeQuery("SELECT procedimento, valorbrutofaturamento, procedimento_id, horariofaturamento FROM faturamentos")) {
                    assertTrue(result.next());
                    assertEquals("Serviço antigo", result.getString(1));
                    assertEquals("120.50", result.getBigDecimal(2).toPlainString());
                    assertNull(result.getObject(3));
                    assertEquals(LocalTime.MIDNIGHT, result.getTime(4).toLocalTime());
                }
                statement.execute("INSERT INTO procedimentos (id, nome, preco, duracao_minutos) VALUES ('00000000-0000-0000-0000-000000000002', 'Design', 100, 30)");
                SQLException exception = assertThrows(SQLException.class, () -> statement.execute("INSERT INTO procedimentos (id, nome, preco, duracao_minutos) VALUES ('00000000-0000-0000-0000-000000000003', ' design ', 100, 30)"));
                assertEquals("23505", exception.getSQLState());
                SQLException invalid = assertThrows(SQLException.class, () -> statement.execute("INSERT INTO procedimentos (id, nome, preco, duracao_minutos) VALUES ('00000000-0000-0000-0000-000000000004', 'Inválido', 0, 30)"));
                assertEquals("23514", invalid.getSQLState());
            } finally {
                // Só remove o schema aleatório criado por este teste.
                statement.execute("DROP SCHEMA " + schema + " CASCADE");
            }
        }
    }
}

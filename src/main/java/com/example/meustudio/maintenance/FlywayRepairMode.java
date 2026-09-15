package com.example.meustudio.maintenance;

import java.util.Map;

import org.flywaydb.core.Flyway;

public final class FlywayRepairMode {

    static final String CONFIRMATION = "repair-schema-history";

    private FlywayRepairMode() {
    }

    public static boolean executeIfRequested(Map<String, String> environment) {
        return executeIfRequested(environment, FlywayRepairMode::repair);
    }

    static boolean executeIfRequested(Map<String, String> environment, RepairExecutor executor) {
        if (!"true".equalsIgnoreCase(environment.get("FLYWAY_REPAIR"))) {
            return false;
        }

        var confirmation = environment.get("FLYWAY_REPAIR_CONFIRM");
        if (!CONFIRMATION.equals(confirmation)) {
            throw new IllegalStateException(
                    "FLYWAY_REPAIR_CONFIRM must be exactly '" + CONFIRMATION + "'");
        }

        var configuration = new DatabaseConfiguration(
                jdbcUrl(environment),
                required(environment, "PGUSER"),
                required(environment, "PGPASSWORD"));

        executor.repair(configuration);
        return true;
    }

    private static void repair(DatabaseConfiguration configuration) {
        Flyway.configure()
                .dataSource(configuration.url(), configuration.user(), configuration.password())
                .locations("classpath:db/migration")
                .load()
                .repair();

        System.out.println("Flyway repair completed; application startup intentionally skipped.");
    }

    private static String jdbcUrl(Map<String, String> environment) {
        return "jdbc:postgresql://%s:%s/%s".formatted(
                required(environment, "PGHOST"),
                required(environment, "PGPORT"),
                required(environment, "PGDATABASE"));
    }

    private static String required(Map<String, String> environment, String name) {
        var value = environment.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable is missing: " + name);
        }
        return value;
    }

    @FunctionalInterface
    interface RepairExecutor {
        void repair(DatabaseConfiguration configuration);
    }

    record DatabaseConfiguration(String url, String user, String password) {
        @Override
        public String toString() {
            return "DatabaseConfiguration[url=%s, user=%s, password=<REDACTED>]".formatted(url, user);
        }
    }
}

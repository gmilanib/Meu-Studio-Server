package com.example.meustudio.maintenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

class FlywayRepairModeTest {

    @Test
    void doesNothingWhenRepairModeIsDisabled() {
        var executed = FlywayRepairMode.executeIfRequested(Map.of(), configuration -> {
            throw new AssertionError("Repair must not run when maintenance mode is disabled");
        });

        assertThat(executed).isFalse();
    }

    @Test
    void refusesRepairWithoutExplicitConfirmation() {
        var environment = validEnvironment();
        environment.remove("FLYWAY_REPAIR_CONFIRM");

        assertThatThrownBy(() -> FlywayRepairMode.executeIfRequested(environment, configuration -> { }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FLYWAY_REPAIR_CONFIRM");
    }

    @Test
    void refusesRepairWhenRequiredDatabaseConfigurationIsMissing() {
        var environment = validEnvironment();
        environment.remove("PGPASSWORD");

        assertThatThrownBy(() -> FlywayRepairMode.executeIfRequested(environment, configuration -> { }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PGPASSWORD");
    }

    @Test
    void executesRepairWithConfigurationBuiltFromEnvironment() {
        var captured = new AtomicReference<FlywayRepairMode.DatabaseConfiguration>();

        var executed = FlywayRepairMode.executeIfRequested(validEnvironment(), captured::set);

        assertThat(executed).isTrue();
        assertThat(captured.get()).isEqualTo(new FlywayRepairMode.DatabaseConfiguration(
                "jdbc:postgresql://postgres.internal:5432/studio",
                "studio_user",
                "secret"));
        assertThat(captured.get().toString()).doesNotContain("secret");
    }

    private static Map<String, String> validEnvironment() {
        var environment = new HashMap<String, String>();
        environment.put("FLYWAY_REPAIR", "true");
        environment.put("FLYWAY_REPAIR_CONFIRM", "repair-schema-history");
        environment.put("PGHOST", "postgres.internal");
        environment.put("PGPORT", "5432");
        environment.put("PGDATABASE", "studio");
        environment.put("PGUSER", "studio_user");
        environment.put("PGPASSWORD", "secret");
        return environment;
    }
}

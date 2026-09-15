package com.example.meustudio;

import com.example.meustudio.maintenance.FlywayRepairMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MeuStudioApplication {

    public static void main(String[] args) {
        if (FlywayRepairMode.executeIfRequested(System.getenv())) {
            return;
        }
        SpringApplication.run(MeuStudioApplication.class, args);
    }
}

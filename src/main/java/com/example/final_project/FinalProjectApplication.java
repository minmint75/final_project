
package com.example.final_project;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinalProjectApplication {

    public static void main(String[] args) {
        loadDotenvIfPresent();
        SpringApplication.run(FinalProjectApplication.class, args);
    }

    private static void loadDotenvIfPresent() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .filename(".env")
                    .directory("./")
                    .ignoreIfMissing()
                    .ignoreIfMalformed()
                    .load();

            for (DotenvEntry e : dotenv.entries()) {
                String key = e.getKey();
                String val = e.getValue();
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, val);
                }
            }
            System.out.println("[Dotenv] Loaded .env (only for missing keys).");
        } catch (Exception ex) {
            System.out.println("[Dotenv] .env not loaded (safe to ignore in prod): " + ex.getMessage());
        }
    }
}

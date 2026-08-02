package com.freelance.freelancepm.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DbFixConfig {

    @Bean
    public CommandLineRunner fixMissingColumns(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                // Forcefully add missing columns if they don't exist
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_token VARCHAR(255);");
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT FALSE;");
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_password_token VARCHAR(100);");
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_password_expires TIMESTAMP;");
                System.out.println("[DB FIX] Successfully verified/added missing columns to users table.");
            } catch (Exception e) {
                System.err.println(
                        "[DB FIX] Could not add columns automatically. They might already exist or the DB rejected it: "
                                + e.getMessage());
            }
        };
    }
}

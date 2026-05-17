package com.wiredbarrack.modulith_exploration.utility;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DatabaseBackupUtility {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseBackupUtility(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PreDestroy
    public void backupDB() {
        String backupPath = "./src/main/resources/backup.sql";
        log.info("Exporting H2 database state to: {}", backupPath);
        jdbcTemplate.execute("SCRIPT TO '" + backupPath + "'");
        log.info("Database state successfully saved!");
    }
}

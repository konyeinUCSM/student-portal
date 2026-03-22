package com.manulife.studentportal.tenant.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Creates a new MySQL schema for a tenant.
 * <p>
 * Tables are created automatically by Hibernate's ddl-auto=update
 * on the first request that targets the new schema.
 * This class only needs to create the empty schema (database).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantSchemaInitializer {

    private final DataSource dataSource;

    /**
     * Creates the MySQL schema (database) for the given tenant.
     * Hibernate's ddl-auto=update will create tables on first access.
     */
    public void initializeSchema(String schemaName) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS `" + schemaName + "`");
            log.info("Tenant schema '{}' created successfully", schemaName);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create schema: " + schemaName, e);
        }
    }

    /**
     * Verifies that a schema exists and is accessible.
     */
    public boolean schemaExists(String schemaName) {
        try (Connection conn = dataSource.getConnection();
             var rs = conn.getMetaData().getCatalogs()) {
            while (rs.next()) {
                if (schemaName.equals(rs.getString(1))) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            log.error("Failed to check schema existence: {}", schemaName, e);
            return false;
        }
    }
}

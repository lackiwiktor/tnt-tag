package me.ponktacology.tag;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public enum Database {
    INSTANCE;

    boolean DEBUG = false;
    private HikariDataSource dataSource;

    Database() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + Constants.Database.DB_HOST + ":" + Constants.Database.DB_PORT + "/" + Constants.Database.DB_NAME);
        config.setUsername(Constants.Database.DB_USERNAME);
        config.setPassword(Constants.Database.DB_PASSWORD);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("rewriteBatchedStatements", true);
        config.addDataSourceProperty("useSSL", false);
        config.addDataSourceProperty("requireSSL", false);
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("encoding", "UTF-8");
        config.addDataSourceProperty("useUnicode", true);
        if (DEBUG) return;
        dataSource = new HikariDataSource(config);
    }

    public int update(String sql, Consumer<PreparedStatement> consumer) {
        if (DEBUG) return 1;
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.prepareStatement(sql);
            consumer.accept(statement);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void query(String sql, Consumer<PreparedStatement> consumer, Consumer<ResultSet> resultSetConsumer) {
        if (DEBUG) return;
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.prepareStatement(sql);
            consumer.accept(statement);
            resultSetConsumer.accept(statement.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

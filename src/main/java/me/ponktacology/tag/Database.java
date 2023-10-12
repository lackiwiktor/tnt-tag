package me.ponktacology.tag;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Consumer;

public enum Database {
    INSTANCE;

    private final HikariDataSource dataSource;

    Database() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + Constants.Database.HOST + ":" + Constants.Database.PORT + "/" + Constants.Database.NAME);
        config.setUsername(Constants.Database.USERNAME);
        config.setPassword(Constants.Database.PASSWORD);
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
        dataSource = new HikariDataSource(config);
    }

    public int update(String sql, Consumer<PreparedStatement> consumer) {
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.prepareStatement(sql);
            consumer.accept(statement);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void query(String sql, Consumer<PreparedStatement> consumer, Consumer<ResultSet> resultSetConsumer) {
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.prepareStatement(sql);
            consumer.accept(statement);
            resultSetConsumer.accept(statement.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

package org.themullers.library.tools;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.themullers.library.db.LibraryDAO;
import org.themullers.library.s3.LibraryOSAO;

import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public abstract class CommandLineTool {

    // this should really be a parameter or something, this is just a base class for one-offs anyway so...
    public final static String CONFIG_FILE_LOCATION = "/Users/mmuller/library.properties";

    protected Logger logger;
    protected Properties config;
    protected LibraryDAO dao;
    protected LibraryOSAO osao;

    public CommandLineTool() throws IOException, SQLException {
        this.logger = (Logger) org.slf4j.LoggerFactory.getLogger(CommandLineTool.class);
        setDefaultLogLevel(Level.ERROR);
        setLogLevel(Level.INFO);
        this.config = getConfig();
        dao = makeDao(config);
        osao = makeOsao(config);
    }

    protected Properties getConfig() throws IOException {
        var config = new Properties();
        try (var reader = new FileReader(CONFIG_FILE_LOCATION)) {
            config.load(reader);
        }
        return config;
    }

    protected void setDefaultLogLevel(Level level) {
        var root = (Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }

    protected void setLogLevel(Level level) {
        logger.setLevel(level);
    }

    protected LibraryDAO makeDao(Properties config) throws SQLException {

        // get connection info from config file
        var url = config.getProperty("spring.datasource.url");
        var username = config.getProperty("spring.datasource.username");
        var password = config.getProperty("spring.datasource.password");

        // create a JDBCTemplate database access object
        var urlWithCredentials = String.format("%s?user=%s&password=%s", url, username, password);
        logger.info("connecting to " + urlWithCredentials);
        var connection = DriverManager.getConnection(urlWithCredentials);
        var jt = new JdbcTemplate(new SingleConnectionDataSource(connection, true));

        return new LibraryDAO(jt);
    }

    protected LibraryOSAO makeOsao(Properties config) {

        var osao = new LibraryOSAO();

        // set connection info from config file
        osao.setAccessKeyId(config.getProperty("object.store.access.key.id"));
        osao.setSecretAccessKey(config.getProperty("object.store.secret.access.key"));
        osao.setBucketName(config.getProperty("object.store.bucket.name"));
        osao.setBucketEndpoint(config.getProperty("object.store.bucket.endpoint"));
        osao.setBucketRegion(config.getProperty("object.store.bucket.region"));

        // initialize after all the properties are set
        osao.init();

        return osao;
    }
}

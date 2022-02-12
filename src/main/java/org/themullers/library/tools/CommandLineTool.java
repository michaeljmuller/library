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

/**
 * This is an abstract base class for command line tools that support the library.
 * It provides a non-springy connection to the database and object store.
 * It also turns down the the default logging so you (mostly) only see messages
 * from the command-line app.
 */
public abstract class CommandLineTool {

    // this should really be a parameter or something, this is just a base class for one-offs anyway so...
    public final static String CONFIG_FILE_LOCATION = "/Users/mmuller/library.properties";

    protected Logger logger;
    protected Properties config;
    protected LibraryDAO dao;
    protected LibraryOSAO osao;

    // constructor
    public CommandLineTool() throws IOException, SQLException {

        // create a logger for use by this object
        this.logger = (Logger) org.slf4j.LoggerFactory.getLogger(CommandLineTool.class);

        // set the default logging level to ERROR and log level for this class to INFO
        setDefaultLogLevel(Level.ERROR);
        setLogLevel(Level.INFO);

        // parse the config file so we know the endpoints and credentials for our DB and object store
        this.config = getConfig();

        // get access objects for the DB and object store
        dao = makeDao(config);
        osao = makeOsao(config);
    }

    /**
     * Parse the application's configuration file.
     * @return  the application's configuration info
     * @throws IOException  thrown if an unexpeted error occurs parsing the config file
     */
    protected Properties getConfig() throws IOException {
        var config = new Properties();
        try (var reader = new FileReader(CONFIG_FILE_LOCATION)) {
            config.load(reader);
        }
        return config;
    }

    /**
     * Set the default log level (for messages logged outside of this class).
     * @param level  the desired default log level
     */
    protected void setDefaultLogLevel(Level level) {
        var root = (Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }

    /**
     * Set the log level for this class.
     * @param level
     */
    protected void setLogLevel(Level level) {
        logger.setLevel(level);
    }

    /**
     * Create an object for use accessing the database.
     * @param config  the application config file with endpoint info and credentials
     * @return  a database access object
     * @throws SQLException  thrown if an unexpected error occurs connecting to the database
     */
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

    /**
     * Create an object for use accessing the object store.
     * @param config  the application config file with endpoint info and credentials
     * @return  an object store access object
     */
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

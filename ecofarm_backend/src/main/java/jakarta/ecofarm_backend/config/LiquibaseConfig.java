package jakarta.ecofarm_backend.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.sql.Connection;
import java.sql.DriverManager;

@ApplicationScoped
public class LiquibaseConfig {

    private static final Logger log = LogManager.getLogger(LiquibaseConfig.class);

    @Inject
    @ConfigProperty(name = "db.url", defaultValue = "jdbc:postgresql://localhost:5432/ecofarm_db")
    private String url;

    @Inject
    @ConfigProperty(name = "db.username", defaultValue = "postgres")
    private String username;

    @Inject
    @ConfigProperty(name = "db.password", defaultValue = "123456")
    private String password;

    @Inject
    @ConfigProperty(name = "liquibase.changelog", defaultValue = "db/changelog/db.changelog-master.xml")
    private String changeLog;

    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object init) {
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                Database database = DatabaseFactory.getInstance()
                        .findCorrectDatabaseImplementation(new JdbcConnection(connection));
                Liquibase liquibase = new Liquibase(
                        changeLog,
                        new ClassLoaderResourceAccessor(Thread.currentThread().getContextClassLoader()),
                        database
                );
                liquibase.update(new Contexts(), new LabelExpression());
                log.info("Liquibase migration completed successfully.");
            }
        } catch (Exception e) {
            log.error("Liquibase migration failed", e);
            throw new RuntimeException("Liquibase migration failed", e);
        }
    }
}


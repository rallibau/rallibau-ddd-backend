package com.rallibau.boundedContext.shared.infrastructure.persistence;

import com.rallibau.shared.infrastructure.persistence.hibernate.HibernateConfigurationFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "presentaciones-entity-manager-factory",
        transactionManagerRef = "presentaciones-transaction_manager",
        basePackages = "com.rallibau.presentaciones")
@EnableTransactionManagement
public class BoundedContextHibernateConfiguration {
    @Value("${presentaciones.database.host}")
    private String DATABASE_HOST;
    @Value("${presentaciones.database.port}")
    private String DATABASE_PORT;
    @Value("${presentaciones.database.name}")
    private String DATABASE_NAME;
    @Value("${presentaciones.database.user}")
    private String DATABASE_USER;
    @Value("${presentaciones.database.password}")
    private String DATABASE_PASSWORD;
    @Value("${presentaciones.database.dialect}")
    private String DIALECT;
    @Value("${presentaciones.database.driver}")
    private String DRIVER;
    @Value("${presentaciones.database.url}")
    private String DATABASE_URL;

    private final HibernateConfigurationFactory factory;
    @Value("${presentaciones.database.name}")
    private String CONTEXT_NAME;

    public BoundedContextHibernateConfiguration(HibernateConfigurationFactory factory) {
        this.factory = factory;
    }

    @Bean("presentaciones-transaction_manager")
    public PlatformTransactionManager hibernateTransactionManager() throws IOException {
        return factory.hibernateTransactionManager(sessionFactory());
    }

    @Bean("presentaciones-session_factory")
    public LocalSessionFactoryBean sessionFactory() throws IOException {
        return factory.sessionFactory(CONTEXT_NAME, dataSource());
    }

    @Bean("presentaciones-data_source")
    public DataSource dataSource() throws IOException {
        return factory.dataSource(
                DATABASE_HOST,
                Integer.parseInt(DATABASE_PORT),
                DATABASE_NAME,
                DATABASE_USER,
                DATABASE_PASSWORD,
                DIALECT,
                DRIVER,
                DATABASE_URL
        );
    }

    @Bean("presentaciones-entity-manager-factory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws IOException {
        return factory.getEntityManagerFactory(CONTEXT_NAME, dataSource());
    }
}

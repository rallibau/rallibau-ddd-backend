package com.rallibau.shared.infrastructure.persistence.hibernate;

import com.rallibau.shared.domain.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;

@Service
public class HibernateConfigurationFactory {

    private static final Logger logger = LogManager.getLogger(HibernateConfigurationFactory.class);

    private final ResourcePatternResolver resourceResolver;

    @Value("${database.auto_ddl}")
    private String AUTO_DDL;
    @Value("${database.show_sql}")
    private String SHOW_SQL;
    private String DIALECT;
    @Value("${base.package}")
    private String BASE_PACKAGE;


    public HibernateConfigurationFactory(ResourcePatternResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    public PlatformTransactionManager hibernateTransactionManager(LocalSessionFactoryBean sessionFactory) {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory.getObject());

        return transactionManager;
    }

    public LocalSessionFactoryBean sessionFactory(String contextName, DataSource dataSource) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setHibernateProperties(hibernateProperties());
        sessionFactory.setPackagesToScan(BASE_PACKAGE.concat(".").concat(contextName));

        List<Resource> mappingFiles = searchMappingFiles(contextName);
        sessionFactory.setMappingLocations(mappingFiles.toArray(new Resource[0]));

        return sessionFactory;
    }

    private List<Resource> searchMappingFiles(String contextName) {
        List<String> modules = subdirectoriesFor(contextName);
        List<String> goodPaths = new ArrayList<>();

        for (String module : modules) {
            String[] files = mappingFilesIn(module + "/infrastructure/persistence/hibernate/");

            for (String file : files) {
                goodPaths.add(module + "/infrastructure/persistence/hibernate/" + file);
            }
        }

        return goodPaths.stream().map(FileSystemResource::new).collect(Collectors.toList());
    }

    private List<String> subdirectoriesFor(String contextName) {
        String BASE_PATH = BASE_PACKAGE.replace(".", "/");
        String path = "./src/" + contextName + "/main/" + BASE_PATH + "/" + contextName + "/";
        String[] files = new File(path).list((current, name) -> new File(current, name).isDirectory());

        if (null == files) {
            path = "./main/" + BASE_PATH + "/" + contextName + "/";
            files = new File(path).list((current, name) -> new File(current, name).isDirectory());
        }
        if (null == files) {
            path = "./app/src/" + contextName + "/main/" + BASE_PATH + "/" + contextName + "/";
            files = new File(path).list((current, name) -> new File(current, name).isDirectory());
        }

        if (null == files) {
            return Collections.emptyList();
        }

        String finalPath = path;

        return Arrays.stream(files).map(file -> finalPath + file).collect(Collectors.toList());
    }

    private String[] mappingFilesIn(String path) {
        String[] files = new File(path).list((current, name) -> new File(current, name).getName().contains(".hbm.xml"));

        if (null == files) {
            return new String[0];
        }

        return files;
    }

    public DataSource dataSource(
            String host,
            Integer port,
            String databaseName,
            String username,
            String password,
            String dialect,
            String driver,
            String url
    ) throws IOException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(
                String.format(
                        url,
                        host,
                        port,
                        databaseName
                )
        );
        this.DIALECT = dialect;
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        initSql(databaseName, dataSource);


        return dataSource;
    }

    private void initSql(String databaseName, BasicDataSource dataSource) throws IOException {
        try {
            Resource sqlResources = resourceResolver.getResource(String.format(
                    "classpath:database/%s.sql",
                    databaseName
            ));

            Scanner scanner = new Scanner(sqlResources.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A");
            if (scanner.hasNext()) {
                String mysqlSentences = new Scanner(sqlResources.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
                dataSource.setConnectionInitSqls(new ArrayList<>(Arrays.asList(mysqlSentences.split(";"))));
            }
        }catch (FileNotFoundException e){
            logger.info("Init scripts for  "+databaseName+ " not found");
        }
    }

    private Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();
        hibernateProperties.put(AvailableSettings.HBM2DDL_AUTO, AUTO_DDL);
        hibernateProperties.put(AvailableSettings.SHOW_SQL, SHOW_SQL);
        hibernateProperties.put(AvailableSettings.DIALECT, DIALECT);

        return hibernateProperties;
    }

    public LocalContainerEntityManagerFactoryBean getEntityManagerFactory(String contextName,DataSource dataSource){
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        entityManagerFactoryBean.setPackagesToScan(BASE_PACKAGE.concat(".").concat(contextName));
        entityManagerFactoryBean.setJpaProperties(hibernateProperties());

        return entityManagerFactoryBean;
    }
}

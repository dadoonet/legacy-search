package fr.pilato.demo.legacysearch.app;

import fr.pilato.demo.legacysearch.domain.Address;
import fr.pilato.demo.legacysearch.domain.Marketing;
import fr.pilato.demo.legacysearch.domain.Person;
import org.dozer.DozerBeanMapper;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Module;
import restx.factory.Provides;

@Module
public class LegacySearchModule implements AutoCloseable {
    final Logger logger = LoggerFactory.getLogger(LegacySearchModule.class);

    private DozerBeanMapper dozerBeanMapper;
    private SessionFactory sessionFactory;

    @Provides
    public DozerBeanMapper dozerBeanMapper() {
        if (dozerBeanMapper == null) {
            logger.debug("creating dozen bean mapper");
            dozerBeanMapper = new DozerBeanMapper();
            logger.debug("dozen bean mapper created");
        }
        return dozerBeanMapper;
    }

    @Provides
    public SessionFactory sessionFactory() {
        if (sessionFactory == null) {
            try {
                logger.debug("creating hibernate session factory");
                Configuration configuration = new Configuration()
                        .addAnnotatedClass(Person.class)
                        .addAnnotatedClass(Address.class)
                        .addAnnotatedClass(Marketing.class);

                if (System.getProperty("restx.mode", "prod").equalsIgnoreCase("dev")) {
                    logger.debug("restx.mode set to dev. Cleaning existing database...");
                    configuration.setProperty("hibernate.hbm2ddl.auto", "create");
                }

                sessionFactory = configuration
                        .configure().buildSessionFactory(
                                new StandardServiceRegistryBuilder().configure().build()
                        );
                logger.debug("hibernate session factory created");
            }
            catch (Throwable ex) {
                logger.error("Initial SessionFactory creation failed." + ex);
                throw new ExceptionInInitializerError(ex);
            }

        }
        return sessionFactory;
    }

    @Override
    public void close() throws Exception {
        if (sessionFactory != null) {
            logger.debug("stopping hibernate session factory");
            sessionFactory.close();
        }

        if (dozerBeanMapper != null) {
            logger.debug("stopping dozen bean mapper");
            dozerBeanMapper.destroy();
        }
    }
}

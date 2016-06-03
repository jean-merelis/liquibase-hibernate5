package liquibase.ext.hibernate.database;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.ext.hibernate.database.connection.HibernateConnection;
import org.hibernate.service.ServiceRegistry;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.spi.PersistenceUnitTransactionType;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import liquibase.ext.hibernate.customfactory.CustomEjb3MetadataFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

/**
 * Database implementation for "ejb3" hibernate configurations. This supports
 * passing an persistence unit name or a
 * {@link liquibase.ext.hibernate.customfactory.CustomEjb3MetadataFactory}
 * implementation
 */
public class HibernateEjb3Database extends HibernateDatabase {

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return conn.getURL().startsWith("hibernate:ejb3:");
    }

    @Override
    protected Metadata buildMetadata(HibernateConnection connection) throws DatabaseException {
        if (isCustomFactoryClass(connection.getPath())) {
            return buildConfigurationFromFactory(connection);
        } else {
            return buildMetadataFromFile(connection);
        }
    }

    /**
     * Build a Configuration object assuming the connection path is a hibernate
     * XML configuration file.
     */
    protected Metadata buildMetadataFromFile(HibernateConnection connection) throws DatabaseException {

        MyHibernatePersistenceProvider persistenceProvider = new MyHibernatePersistenceProvider();
        final EntityManagerFactoryBuilderImpl builder = (EntityManagerFactoryBuilderImpl) persistenceProvider.getEntityManagerFactoryBuilderOrNull(connection.getPath(), null, null);

        EntityManagerFactory emf = builder.build();
        String dialectString = (String) emf.getProperties().get(AvailableSettings.DIALECT);

        ServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, configureDialect(dialectString))
                .applySetting(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true")
                .build();

        MetadataSources sources = new MetadataSources(standardRegistry);

        Iterator<ManagedType<?>> it = emf.getMetamodel().getManagedTypes().iterator();
        while (it.hasNext()) {
            Class<?> javaType = it.next().getJavaType();
            if (javaType == null) {
                continue;
            }
            sources.addAnnotatedClass(javaType);
        }

        Package[] packages = Package.getPackages();
        for (Package p : packages) {
            sources.addPackage(p);
        }

        MetadataBuilder metadataBuilder = sources.getMetadataBuilder();
        metadataBuilder.enableNewIdentifierGeneratorSupport(true);
        configureImplicitNamingStrategy(metadataBuilder, (String) emf.getProperties().get(AvailableSettings.IMPLICIT_NAMING_STRATEGY));
        configurePhysicalNamingStrategy(metadataBuilder, (String) emf.getProperties().get(AvailableSettings.PHYSICAL_NAMING_STRATEGY));

        return metadataBuilder.build();
    }

    /**
     * Build a Configuration object assuming the connection path is a
     * {@link CustomEjb3MetadataFactory} class name
     */
    protected Metadata buildConfigurationFromFactory(HibernateConnection connection) throws DatabaseException {
        try {
            return ((CustomEjb3MetadataFactory) Class.forName(connection.getPath()).newInstance()).getMetadata(this, connection);
        } catch (InstantiationException e) {
            throw new DatabaseException(e);
        } catch (IllegalAccessException e) {
            throw new DatabaseException(e);
        } catch (ClassNotFoundException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Return true if the given path is a {@link CustomEjb3MetadataFactory}
     */
    protected boolean isCustomFactoryClass(String path) {
        if (path.contains("/")) {
            return false;
        }

        try {
            Class<?> clazz = Class.forName(path);
            return CustomEjb3MetadataFactory.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public String getShortName() {
        return "hibernateEjb3";
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "Hibernate EJB3";
    }

    private static class MyHibernatePersistenceProvider extends HibernatePersistenceProvider {

        private void setField(final Object obj, String fieldName, final Object value) throws Exception {
            final Field declaredField;

            declaredField = obj.getClass().getDeclaredField(fieldName);
            AccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public Object run() {
                    boolean wasAccessible = declaredField.isAccessible();
                    try {
                        declaredField.setAccessible(true);
                        declaredField.set(obj, value);
                        return null;
                    } catch (Exception ex) {
                        throw new IllegalStateException("Cannot invoke method get", ex);
                    } finally {
                        declaredField.setAccessible(wasAccessible);
                    }
                }
            });
        }

        @Override
        protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilderOrNull(String persistenceUnitName, Map properties, ClassLoader providedClassLoader) {
            return super.getEntityManagerFactoryBuilderOrNull(persistenceUnitName, properties, providedClassLoader);
        }

        @Override
        protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilder(PersistenceUnitDescriptor persistenceUnitDescriptor, Map integration, ClassLoader providedClassLoader) {
            try {
                setField(persistenceUnitDescriptor, "jtaDataSource", null);
                setField(persistenceUnitDescriptor, "transactionType", PersistenceUnitTransactionType.RESOURCE_LOCAL);
            } catch (Exception ex) {
                Logger.getLogger(HibernateEjb3Database.class.getName()).log(Level.SEVERE, null, ex);
            }
            return super.getEntityManagerFactoryBuilder(persistenceUnitDescriptor, integration, providedClassLoader);
        }
    }
}

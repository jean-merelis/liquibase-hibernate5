package liquibase.ext.hibernate.database;

/*
 * #%L
 * Liquibase Hibernate 5 Integration
 * %%
 * Copyright (C) 2016 Liquibase.org
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.HashMap;
import java.util.Map;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.ext.hibernate.database.connection.HibernateConnection;
import liquibase.ext.hibernate.database.connection.HibernateDriver;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;

/**
 * Base class for all Hibernate Databases. This extension interacts with
 * Hibernate by creating standard liquibase.database.Database implementations
 * that bridge what Liquibase expects and the Hibernate APIs.
 */
public abstract class HibernateDatabase extends AbstractJdbcDatabase {

    protected static final Logger LOG = LogFactory.getLogger("liquibase-hibernate");

    private Metadata metadata;

    private Dialect dialect;

    private boolean indexesForForeignKeys = false;
    public static final String DEFAULT_SCHEMA = "HIBERNATE";

    private Map<String, String> urlParams = new HashMap<String, String>();

    public HibernateDatabase() {
        setDefaultCatalogName(DEFAULT_SCHEMA);
        setDefaultSchemaName(DEFAULT_SCHEMA);
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        super.setConnection(conn);

        try {
            LOG.info("Reading hibernate configuration " + getConnection().getURL());

            this.metadata = buildMetadata(((HibernateConnection) ((JdbcConnection) conn).getUnderlyingConnection()));

            afterSetup();
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    /**
     * Return the dialect used by hibernate
     */
    protected Dialect configureDialect(String dialectString) throws DatabaseException {
        dialectString = ((HibernateConnection) ((JdbcConnection) getConnection()).getUnderlyingConnection()).getProperties().getProperty(AvailableSettings.DIALECT, dialectString);
        if (dialectString != null) {
            try {
                dialect = (Dialect) Class.forName(dialectString).newInstance();
                LOG.info("Using dialect " + dialectString);
            } catch (Exception e) {
                throw new DatabaseException(e);
            }
        } else {
            LOG.info("Could not determine hibernate dialect, using HibernateGenericDialect");
            dialect = new HibernateGenericDialect();
        }

        return dialect;
    }



    protected void configureNewIdentifierGeneratorSupport(MetadataBuilder builder, String value) throws DatabaseException {
        String _value;
        _value = ((HibernateConnection) ((JdbcConnection) getConnection()).getUnderlyingConnection()).getProperties().getProperty(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, value);

        try {
            if (_value != null) {
                builder.enableNewIdentifierGeneratorSupport(Boolean.valueOf(_value));
            }
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    protected void configurePhysicalNamingStrategy(MetadataBuilder builder, String physicalNamingStrategy) throws DatabaseException {
        String namingStrategy;
        namingStrategy = ((HibernateConnection) ((JdbcConnection) getConnection()).getUnderlyingConnection()).getProperties().getProperty(AvailableSettings.PHYSICAL_NAMING_STRATEGY, physicalNamingStrategy);

        try {
            if (namingStrategy != null) {
                builder.applyPhysicalNamingStrategy((PhysicalNamingStrategy)Class.forName(namingStrategy).newInstance());
            }
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    protected void configureImplicitNamingStrategy(MetadataBuilder builder, String implicitNamingStrategy) throws DatabaseException {
        String namingStrategy;
        namingStrategy = ((HibernateConnection) ((JdbcConnection) getConnection()).getUnderlyingConnection()).getProperties().getProperty(AvailableSettings.IMPLICIT_NAMING_STRATEGY, implicitNamingStrategy);

        try {
            if (namingStrategy != null) {
                switch(namingStrategy){
                    case "default":
                    case "jpa":
                        builder.applyImplicitNamingStrategy(org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl.INSTANCE);
                        break;
                    case "legacy-hbm":
                        builder.applyImplicitNamingStrategy(org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyHbmImpl.INSTANCE);
                        break;
                    case "legacy-jpa":
                        builder.applyImplicitNamingStrategy(org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl.INSTANCE);
                        break;
                    case "component-path":
                        builder.applyImplicitNamingStrategy(org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl.INSTANCE);
                        break;
                    default:
                        builder.applyImplicitNamingStrategy((ImplicitNamingStrategy)Class.forName(namingStrategy).newInstance());
                        break;
                }

            }
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Perform any post-configuration setting logic.
     */
    protected void afterSetup() {
        if (dialect instanceof MySQLDialect) {
            indexesForForeignKeys = true;
        }
    }

    /**
     * Concrete implementations use this method to create the hibernate
     * Configuration object based on the passed URL
     */
    //  protected abstract Configuration buildConfiguration(HibernateConnection conn) throws DatabaseException;
    protected abstract Metadata buildMetadata(HibernateConnection connection) throws DatabaseException;

    public boolean requiresPassword() {
        return false;
    }

    public boolean requiresUsername() {
        return false;
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("hibernate")) {
            return HibernateDriver.class.getName();
        }
        return null;
    }

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean createsIndexesForForeignKeys() {
        return indexesForForeignKeys;
    }

    @Override
    public Integer getDefaultPort() {
        return 0;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

//    public Configuration getConfiguration() throws DatabaseException {
//        return configuration;
//    }
    public Metadata getMetadata() throws DatabaseException {
        return metadata;
    }

    public Dialect getDialect() {
        return dialect;
    }

    @Override
    protected String getConnectionCatalogName() throws DatabaseException {
        return getDefaultCatalogName();
    }

    @Override
    protected String getConnectionSchemaName() {
        return getDefaultSchemaName();
    }

    @Override
    public String getDefaultSchemaName() {
        return DEFAULT_SCHEMA;
    }

    @Override
    public String getDefaultCatalogName() {
        return DEFAULT_SCHEMA;
    }

    @Override
    public boolean isSafeToRunUpdate() throws DatabaseException {
        return true;
    }

    @Override
    public boolean isCaseSensitive() {
        return false;
    }

    @Override
    public boolean supportsSchemas() {
        return true;
    }

    @Override
    public boolean supportsCatalogs() {
        return false;
    }
}

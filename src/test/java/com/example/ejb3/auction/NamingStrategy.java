package com.example.ejb3.auction;

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

import java.util.Locale;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitJoinColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitJoinTableNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 *
 * @author jean
 */
public class NamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl implements PhysicalNamingStrategy {

    public static final NamingStrategy INSTANCE = new NamingStrategy();

    protected static String addUnderscores(String name) {
        StringBuilder buf = new StringBuilder(name.replace('.', '_'));
        for (int i = 1; i < buf.length() - 1; i++) {
            if (Character.isLowerCase(buf.charAt(i - 1))
                    && Character.isUpperCase(buf.charAt(i))
                    && Character.isLowerCase(buf.charAt(i + 1))) {
                buf.insert(i++, '_');
            }
        }

        return buf.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public Identifier determineJoinColumnName(ImplicitJoinColumnNameSource source) {
        if (source.getAttributePath() != null) {
            return toIdentifier(
                    (source.getNature() == ImplicitJoinColumnNameSource.Nature.ELEMENT_COLLECTION
                            ? addUnderscores(transformEntityName( source.getEntityNaming() ))
                            : addUnderscores(source.getAttributePath().getFullPath()))
                    + '_' + source.getReferencedColumnName().getText(),
                    source.getBuildingContext()
            );
        }
        if (source.getNature() != ImplicitJoinColumnNameSource.Nature.ELEMENT_COLLECTION) {
            String name = transformEntityName(source.getEntityNaming())
                    + '_'
                    + source.getReferencedColumnName().getText();
            return toIdentifier(
                    addUnderscores(name),
                    source.getBuildingContext()
            );
        }
        return super.determineJoinColumnName(source);
    }

    @Override
    public Identifier determineJoinTableName(ImplicitJoinTableNameSource source) {
        if (source.getAssociationOwningAttributePath() != null) {
            final String name = source.getOwningPhysicalTableName()
                    + '_'
                    + transformAttributePath(source.getAssociationOwningAttributePath());

            return toIdentifier(name, source.getBuildingContext());
        }

        return super.determineJoinTableName(source);
    }



    // Physical implementation
    
    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return jdbcEnvironment.getIdentifierHelper().toIdentifier(
                addUnderscores(name.getText()),
                name.isQuoted()
        );
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return jdbcEnvironment.getIdentifierHelper().toIdentifier(
                addUnderscores(name.getText()),
                name.isQuoted()
        );
    }
}

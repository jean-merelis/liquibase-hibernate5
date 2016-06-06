package liquibase.ext.hibernate.customfactory;

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

import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.ext.hibernate.database.connection.HibernateConnection;
import org.hibernate.boot.Metadata;

/**
 * Implement this interface to dynamically generate a hibernate:ejb3 configuration.
 * For example, if you create a class called com.example.hibernate.MyConfig, specify a url of hibernate:ejb3:com.example.hibernate.MyConfig.
 */
public interface CustomEjb3MetadataFactory {

    /*
     * Create a hibernate Configuration for the given database and connection.
     */
    Metadata getMetadata(HibernateDatabase hibernateDatabase, HibernateConnection connection);

}

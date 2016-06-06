package liquibase.ext.hibernate.database.connection;

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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HibernateConnectionTest {

    private final String FILE_PATH = "/path/to/file.ext";

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testHibernateUrlSimple() {
        HibernateConnection conn = new HibernateConnection("hibernate:classic:" + FILE_PATH);
        Assert.assertEquals("hibernate:classic", conn.getPrefix());
        assertEquals(FILE_PATH, conn.getPath());
        assertEquals(0, conn.getProperties().size());
    }

    @Test
    public void testHibernateUrlWithProperties() {
        HibernateConnection conn = new HibernateConnection("hibernate:classic:" + FILE_PATH + "?foo=bar&name=John+Doe");
        assertEquals("hibernate:classic", conn.getPrefix());
        assertEquals(FILE_PATH, conn.getPath());
        assertEquals(2, conn.getProperties().size());
        assertEquals("bar", conn.getProperties().getProperty("foo", null));
        assertEquals("John Doe", conn.getProperties().getProperty("name", null));
    }

    @Test
    public void testEjb3UrlSimple() {
        HibernateConnection conn = new HibernateConnection("hibernate:ejb3:" + FILE_PATH);
        assertEquals("hibernate:ejb3", conn.getPrefix());
        assertEquals(FILE_PATH, conn.getPath());
        assertEquals(0, conn.getProperties().size());
    }

    @Test
    public void testEjb3UrlWithProperties() {
        HibernateConnection conn = new HibernateConnection("hibernate:ejb3:" + FILE_PATH + "?foo=bar&name=John+Doe");
        assertEquals("hibernate:ejb3", conn.getPrefix());
        assertEquals(FILE_PATH, conn.getPath());
        assertEquals(2, conn.getProperties().size());
        assertEquals("bar", conn.getProperties().getProperty("foo", null));
        assertEquals("John Doe", conn.getProperties().getProperty("name", null));
    }
}

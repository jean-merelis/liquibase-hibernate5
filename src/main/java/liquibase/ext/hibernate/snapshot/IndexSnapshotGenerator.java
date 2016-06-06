package liquibase.ext.hibernate.snapshot;

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

import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.util.Iterator;

public class IndexSnapshotGenerator extends HibernateSnapshotGenerator {

    public IndexSnapshotGenerator() {
        super(Index.class, new Class[]{Table.class, ForeignKey.class, UniqueConstraint.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (example.getSnapshotId() != null) {
            return example;
        }
        Table table = ((Index) example).getTable();
        org.hibernate.mapping.Table hibernateTable = findHibernateTable(table, snapshot);
        if (hibernateTable == null) {
            return example;
        }
        Iterator indexIterator = hibernateTable.getIndexIterator();
        while (indexIterator.hasNext()) {
            org.hibernate.mapping.Index hibernateIndex = (org.hibernate.mapping.Index) indexIterator.next();
            Index index = new Index();
            index.setTable(table);
            index.setName(hibernateIndex.getName());
            Iterator columnIterator = hibernateIndex.getColumnIterator();
            while (columnIterator.hasNext()) {
                org.hibernate.mapping.Column hibernateColumn = (org.hibernate.mapping.Column) columnIterator.next();
                index.getColumns().add(new Column(hibernateColumn.getName()).setRelation(table));
            }

            if (index.getColumnNames().equalsIgnoreCase(((Index) example).getColumnNames())) {
                LOG.info("Found index " + index.getName());
                table.getIndexes().add(index);
                return index;
            }
        }
        return example;

    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(Index.class)) {
            return;
        }
        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            org.hibernate.mapping.Table hibernateTable = findHibernateTable(table, snapshot);
            if (hibernateTable == null) {
                return;
            }
            Iterator indexIterator = hibernateTable.getIndexIterator();
            while (indexIterator.hasNext()) {
                org.hibernate.mapping.Index hibernateIndex = (org.hibernate.mapping.Index) indexIterator.next();
                Index index = new Index();
                index.setTable(table);
                index.setName(hibernateIndex.getName());
                Iterator columnIterator = hibernateIndex.getColumnIterator();
                while (columnIterator.hasNext()) {
                    org.hibernate.mapping.Column hibernateColumn = (org.hibernate.mapping.Column) columnIterator.next();
                    index.getColumns().add(new Column(hibernateColumn.getName()).setRelation(table));
                }
                LOG.info("Found index " + index.getName());
                table.getIndexes().add(index);
            }
        }
    }

}

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
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;

import java.util.Iterator;

public class UniqueConstraintSnapshotGenerator extends HibernateSnapshotGenerator {

    public UniqueConstraintSnapshotGenerator() {
        super(UniqueConstraint.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        return example;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(UniqueConstraint.class)) {
            return;
        }

        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            org.hibernate.mapping.Table hibernateTable = findHibernateTable(table, snapshot);
            if (hibernateTable == null) {
                return;
            }
            Iterator uniqueIterator = hibernateTable.getUniqueKeyIterator();
            while (uniqueIterator.hasNext()) {
                org.hibernate.mapping.UniqueKey hibernateUnique = (org.hibernate.mapping.UniqueKey) uniqueIterator.next();

                UniqueConstraint uniqueConstraint = new UniqueConstraint();
                uniqueConstraint.setTable(table);
                Iterator columnIterator = hibernateUnique.getColumnIterator();
                int i = 0;
                while (columnIterator.hasNext()) {
                    org.hibernate.mapping.Column hibernateColumn = (org.hibernate.mapping.Column) columnIterator.next();
                    uniqueConstraint.addColumn(i, new Column(hibernateColumn.getName()).setRelation(table));
                    i++;
                }

                Index index = getBackingIndex(uniqueConstraint, hibernateTable, snapshot);
                uniqueConstraint.setBackingIndex(index);

                LOG.info("Found unique constraint " + uniqueConstraint.toString());
                table.getUniqueConstraints().add(uniqueConstraint);
            }
            Iterator columnIterator = hibernateTable.getColumnIterator();
            while (columnIterator.hasNext()) {
                org.hibernate.mapping.Column column = (org.hibernate.mapping.Column) columnIterator.next();
                if (column.isUnique()) {
                    UniqueConstraint uniqueConstraint = new UniqueConstraint();
                    uniqueConstraint.setTable(table);
                    String name = "UC_" + table.getName().toUpperCase() + column.getName().toUpperCase() + "_COL";
                    if (name.length() > 64) {
                        name = name.substring(0, 63);
                    }
                    uniqueConstraint.addColumn(0, new Column(column.getName()).setRelation(table));
                    uniqueConstraint.setName(name);
                    LOG.info("Found unique constraint " + uniqueConstraint.toString());
                    table.getUniqueConstraints().add(uniqueConstraint);

                    Index index = getBackingIndex(uniqueConstraint, hibernateTable, snapshot);
                    uniqueConstraint.setBackingIndex(index);

                }
            }
        }
    }

    protected Index getBackingIndex(UniqueConstraint uniqueConstraint, org.hibernate.mapping.Table hibernateTable, DatabaseSnapshot snapshot) {
        Index index = new Index();
        index.setTable(uniqueConstraint.getTable());
        index.setColumns(uniqueConstraint.getColumns());
        index.setUnique(true);

        return index;
    }

}

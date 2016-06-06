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

import java.util.Collection;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;

import java.util.Iterator;
import org.hibernate.boot.spi.MetadataImplementor;

public class ForeignKeySnapshotGenerator extends HibernateSnapshotGenerator {

    public ForeignKeySnapshotGenerator() {
        super(ForeignKey.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        return example;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(ForeignKey.class)) {
            return;
        }
        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            HibernateDatabase database = (HibernateDatabase) snapshot.getDatabase();
            MetadataImplementor metadata = (MetadataImplementor) database.getMetadata();

            Collection<org.hibernate.mapping.Table> tmapp = metadata.collectTableMappings();
            Iterator<org.hibernate.mapping.Table> tableMappings = tmapp.iterator();
            while (tableMappings.hasNext()) {
                org.hibernate.mapping.Table hibernateTable = (org.hibernate.mapping.Table) tableMappings.next();
                Iterator fkIterator = hibernateTable.getForeignKeyIterator();
                while (fkIterator.hasNext()) {
                    org.hibernate.mapping.ForeignKey hibernateForeignKey = (org.hibernate.mapping.ForeignKey) fkIterator.next();
                    Table currentTable = new Table().setName(hibernateTable.getName());
                    currentTable.setSchema(hibernateTable.getCatalog(), hibernateTable.getSchema());

                    org.hibernate.mapping.Table hibernateReferencedTable = hibernateForeignKey.getReferencedTable();
                    Table referencedTable = new Table().setName(hibernateReferencedTable.getName());
                    referencedTable.setSchema(hibernateReferencedTable.getCatalog(), hibernateReferencedTable.getSchema());

                    if (hibernateForeignKey.isPhysicalConstraint()) {
                        ForeignKey fk = new ForeignKey();
                        fk.setName(hibernateForeignKey.getName());
                        fk.setPrimaryKeyTable(referencedTable);
                        fk.setForeignKeyTable(currentTable);
                        for (Object column : hibernateForeignKey.getColumns()) {
                            fk.addForeignKeyColumn(new liquibase.structure.core.Column(((org.hibernate.mapping.Column) column).getName()));
                        }
                        for (Object column : hibernateForeignKey.getReferencedColumns()) {
                            fk.addPrimaryKeyColumn(new liquibase.structure.core.Column(((org.hibernate.mapping.Column) column).getName()));
                        }
                        if (fk.getPrimaryKeyColumns() == null || fk.getPrimaryKeyColumns().isEmpty()) {
                            for (Object column : hibernateReferencedTable.getPrimaryKey().getColumns()) {
                                fk.addPrimaryKeyColumn(new liquibase.structure.core.Column(((org.hibernate.mapping.Column) column).getName()));
                            }
                        }

                        fk.setDeferrable(false);
                        fk.setInitiallyDeferred(false);

//			Index index = new Index();
//			index.setName("IX_" + fk.getName());
//			index.setTable(fk.getForeignKeyTable());
//			index.setColumns(fk.getForeignKeyColumns());
//			fk.setBackingIndex(index);
//			table.getIndexes().add(index);

                        if (DatabaseObjectComparatorFactory.getInstance().isSameObject(currentTable, table, database)) {
                            table.getOutgoingForeignKeys().add(fk);
                            table.getSchema().addDatabaseObject(fk);
                        }
                    }
                }
            }
        }
    }

}

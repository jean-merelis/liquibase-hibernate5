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

import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.core.UnknownType;
import liquibase.exception.DatabaseException;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.SqlUtil;
import liquibase.util.StringUtils;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.mapping.*;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hibernate.boot.Metadata;
import org.hibernate.id.ExportableColumn;

/**
 * Columns are snapshotted along with with Tables in
 * {@link TableSnapshotGenerator} but this class needs to be here to keep the
 * default ColumnSnapshotGenerator from running. Ideally the column logic would
 * be moved out of the TableSnapshotGenerator to better work in situations where
 * the object types to snapshot are being controlled, but that is not the case
 * yet.
 */
public class ColumnSnapshotGenerator extends HibernateSnapshotGenerator {

    private final static Pattern pattern = Pattern.compile("([^\\(]*)\\s*\\(?\\s*(\\d*)?\\s*,?\\s*(\\d*)?\\s*([^\\(]*?)\\)?");

    public ColumnSnapshotGenerator() {
        super(Column.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Column column = (Column) example;
        if (column.getType() == null) { //not the actual full version found with the table
            if (column.getRelation() == null) {
                throw new InvalidExampleException("No relation set on " + column);
            }
            Relation relation = snapshot.get(column.getRelation());
            if (relation != null) {
                for (Column columnSnapshot : relation.getColumns()) {
                    if (columnSnapshot.getName().equalsIgnoreCase(column.getName())) {
                        return columnSnapshot;
                    }
                }
            }
            snapshotColumn((Column) example, snapshot);
            return example; //did not find it
        } else {
            return example;
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (foundObject instanceof Table) {
            org.hibernate.mapping.Table hibernateTable = findHibernateTable(foundObject, snapshot);
            if (hibernateTable == null) {
                return;
            }

            Iterator columnIterator = hibernateTable.getColumnIterator();
            while (columnIterator.hasNext()) {
                org.hibernate.mapping.Column hibernateColumn = (org.hibernate.mapping.Column) columnIterator.next();
                Column column = new Column();
                column.setName(hibernateColumn.getName());
                column.setRelation((Table) foundObject);

                snapshotColumn(column, snapshot);

                ((Table) foundObject).getColumns().add(column);

            }
        }
    }

    protected void snapshotColumn(Column column, DatabaseSnapshot snapshot) throws DatabaseException {
        HibernateDatabase database = (HibernateDatabase) snapshot.getDatabase();

        org.hibernate.mapping.Table hibernateTable = findHibernateTable(column.getRelation(), snapshot);
        if (hibernateTable == null) {
            return;
        }

        Dialect dialect = database.getDialect();

        Metadata metadata = database.getMetadata();

        Iterator columnIterator = hibernateTable.getColumnIterator();
        while (columnIterator.hasNext()) {
            org.hibernate.mapping.Column hibernateColumn = (org.hibernate.mapping.Column) columnIterator.next();
            if (hibernateColumn.getName().equalsIgnoreCase(column.getName())) {

                String hibernateType = hibernateColumn.getSqlType(dialect, metadata);
                DataType dataType = toDataType(hibernateType, hibernateColumn.getSqlTypeCode());
                if (dataType == null) {
                    throw new DatabaseException("Unable to find column data type for column " + hibernateColumn.getName());
                }

                column.setType(dataType);
                LOG.info("Found column " + column.getName() + " " + column.getType().toString());

                column.setRemarks(hibernateColumn.getComment());
                if (hibernateColumn.getValue() instanceof SimpleValue) {
                    DataType parseType;
                    if (DataTypeFactory.getInstance().from(dataType, database) instanceof UnknownType) {
                        parseType = new DataType(((SimpleValue) hibernateColumn.getValue()).getTypeName());
                    } else {
                        parseType = dataType;
                    }
                    column.setDefaultValue(SqlUtil.parseValue(
                            snapshot.getDatabase(),
                            hibernateColumn.getDefaultValue(),
                            parseType));
                } else {
                    column.setDefaultValue(hibernateColumn.getDefaultValue());
                }
                column.setNullable(hibernateColumn.isNullable());
                column.setCertainDataType(false);

                org.hibernate.mapping.PrimaryKey hibernatePrimaryKey = hibernateTable.getPrimaryKey();
                if (hibernatePrimaryKey != null) {
                    boolean isPrimaryKeyColumn = false;
                    for (org.hibernate.mapping.Column pkColumn : (List<org.hibernate.mapping.Column>) hibernatePrimaryKey.getColumns()) {
                        if (pkColumn.getName().equalsIgnoreCase(hibernateColumn.getName())) {
                            isPrimaryKeyColumn = true;
                            break;
                        }
                    }

                    if (isPrimaryKeyColumn) {
                        String identifierGeneratorStrategy;

                        if (hibernateColumn instanceof ExportableColumn) {
                            //nothing
                        } else {

                            identifierGeneratorStrategy = hibernateColumn.getValue().isSimpleValue()
                                    ? ((SimpleValue) hibernateColumn.getValue()).getIdentifierGeneratorStrategy() : null;

                            if (("native".equalsIgnoreCase(identifierGeneratorStrategy) || "identity".equalsIgnoreCase(identifierGeneratorStrategy))) {
                                if (PostgreSQL81Dialect.class.isAssignableFrom(dialect.getClass())) {
                                    column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
                                    String sequenceName = (column.getRelation().getName() + "_" + column.getName() + "_seq").toLowerCase();
                                    column.setDefaultValue(new DatabaseFunction("nextval('" + sequenceName + "'::regclass)"));
                                } else if (dialect.getNativeIdentifierGeneratorClass().equals(IdentityGenerator.class)) {
                                    column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
                                }
                            } else if ("org.hibernate.id.enhanced.SequenceStyleGenerator".equals(identifierGeneratorStrategy)) {
                                Properties prop = ((SimpleValue) hibernateColumn.getValue()).getIdentifierGeneratorProperties();
                                if (prop.get("sequence_name") == null)
                                   column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
                            }
                        }
                    }
                }
                return;
            }
        }
    }

    protected DataType toDataType(String hibernateType, Integer sqlTypeCode) throws DatabaseException {
        Matcher matcher = pattern.matcher(hibernateType);
        if (!matcher.matches()) {
            return null;
        }
        DataType dataType = new DataType(matcher.group(1));
        if (matcher.group(3).isEmpty()) {
            if (!matcher.group(2).isEmpty()) {
                dataType.setColumnSize(Integer.parseInt(matcher.group(2)));
            }
        } else {
            dataType.setColumnSize(Integer.parseInt(matcher.group(2)));
            dataType.setDecimalDigits(Integer.parseInt(matcher.group(3)));
        }

        String extra = StringUtils.trimToNull(matcher.group(4));
        if (extra != null) {
            if (extra.equalsIgnoreCase("char")) {
                dataType.setColumnSizeUnit(DataType.ColumnSizeUnit.CHAR);
            }
        }

        dataType.setDataTypeId(sqlTypeCode);
        return dataType;
    }

}

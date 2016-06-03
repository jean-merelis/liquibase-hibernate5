package liquibase.ext.hibernate.database;

import java.util.Iterator;
import java.util.Set;
import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.*;

public class HibernateEjb3DatabaseTest {

    @Test
    public void simpleEjb3Url() throws Exception {
        String url = "hibernate:ejb3:auction";
        Database database = CommandLineUtils.createDatabaseObject(this.getClass().getClassLoader(), url, null, null, null, null, null, false, false, null, null, null, null, null, null, null);

        assertNotNull(database);

        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(CatalogAndSchema.DEFAULT, database, new SnapshotControl(database));

        assertEjb3HibernateMapped(snapshot);
    }

    public static void assertEjb3HibernateMapped(DatabaseSnapshot snapshot) {
        assertThat(snapshot.get(Table.class), containsInAnyOrder(
                hasProperty("name", is("Bid")),
                hasProperty("name", is("Watcher")),
                hasProperty("name", is("User")),
                hasProperty("name", is("user_phone")), 
                hasProperty("name", is("AuctionInfo")),
                hasProperty("name", is("AuctionItem")),
                hasProperty("name", is("Item")),
                hasProperty("name", is("AuditedItem")),
                hasProperty("name", is("AuditedItem_AUD")),
                hasProperty("name", is("REVINFO")),
                hasProperty("name", is("WatcherSeqTable"))));

        Table bidTable = (Table) snapshot.get(new Table().setName("bid").setSchema(new Schema()));
        Table auctionInfoTable = (Table) snapshot.get(new Table().setName("auctioninfo").setSchema(new Schema()));
        Table auctionItemTable = (Table) snapshot.get(new Table().setName("auctionitem").setSchema(new Schema()));

        assertThat(bidTable.getColumns(), containsInAnyOrder(
                hasProperty("name", is("id")),
                hasProperty("name", is("item_id")),
                hasProperty("name", is("amount")),
                hasProperty("name", is("datetime")),
                hasProperty("name", is("bidder_id")),
                hasProperty("name", is("DTYPE"))
        ));

        assertTrue(bidTable.getColumn("id").isAutoIncrement());
        assertFalse(auctionInfoTable.getColumn("id").isAutoIncrement());
        assertFalse(bidTable.getColumn("datetime").isNullable());
        assertTrue(auctionItemTable.getColumn("ends").isNullable());

        assertThat(bidTable.getPrimaryKey().getColumnNames(), is("id"));

        assertThat(bidTable.getOutgoingForeignKeys(), containsInAnyOrder(
                allOf(
                        hasProperty("primaryKeyColumns", hasToString("[AuctionItem.id]")),
                        hasProperty("foreignKeyColumns", hasToString("[Bid.item_id]")),
                        hasProperty("primaryKeyTable", hasProperty("name", is("AuctionItem")))
                ),
                allOf(
                        hasProperty("primaryKeyColumns", hasToString("[User.id]")),
                        hasProperty("foreignKeyColumns", hasToString("[Bid.bidder_id]")),
                        hasProperty("primaryKeyTable", hasProperty("name", is("User")))
                )
        ));
    }

    @Test
    public void ejb3UrlWithNamingStrategy() throws Exception {
        String url = "hibernate:ejb3:auction?hibernate.implicit_naming_strategy=com.example.ejb3.auction.NamingStrategy&hibernate.physical_naming_strategy=com.example.ejb3.auction.NamingStrategy";
        Database database = CommandLineUtils.createDatabaseObject(this.getClass().getClassLoader(), url, null, null, null, null, null, false, false, null, null, null, null, null, null, null);

        assertNotNull(database);

        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(CatalogAndSchema.DEFAULT, database, new SnapshotControl(database));
        Set<Table> allTables = snapshot.get(Table.class);
        assertThat(allTables, containsInAnyOrder(
                hasProperty("name", is("bid")),
                hasProperty("name", is("watcher")),
                hasProperty("name", is("user")),
                hasProperty("name", is("user_phone")),
                hasProperty("name", is("auction_info")),
                hasProperty("name", is("auction_item")),
                hasProperty("name", is("item")),
                hasProperty("name", is("audited_item")),
                hasProperty("name", is("audited_item_aud")),
                hasProperty("name", is("revinfo")),
                hasProperty("name", is("WatcherSeqTable"))));

        Table tb = getByName(allTables, "user");
        assertThat(tb.getColumns(), containsInAnyOrder(
                hasProperty("name",is("id")),
                hasProperty("name",is("user_name")),
                hasProperty("name",is("password")),
                hasProperty("name",is("email")),
                hasProperty("name",is("first_name")),
                hasProperty("name",is("initial")),
                hasProperty("name",is("last_name"))

        ));

    }

    private Table getByName(Set<Table> allTables, String name){
        Iterator<Table> iter = allTables.iterator();
        while(iter.hasNext()){
            Table tb = iter.next();
            if(tb.getName().equals(name))
                return tb;
        }
        return null;
    }
}

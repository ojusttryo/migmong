package com.github.ojusttryo.migmong.dao;

import static com.github.ojusttryo.migmong.common.Constants.TEST_MIGRATION_COLLECTION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bson.Document;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.ojusttryo.migmong.AbstractMigrationTest;
import com.github.ojusttryo.migmong.migration.MigrationEntry;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * @author lstolowski
 * @since 10.12.14
 */
public class MigrationEntryIndexDaoTest extends AbstractMigrationTest
{
    private static final String CHANGE_ID_INDEX_NAME = "changeId_1";

    private ChangeEntryIndexDao dao = new ChangeEntryIndexDao(TEST_MIGRATION_COLLECTION);


    @Test
    public void shouldCreateRequiredUniqueIndex()
    {
        MongoClient mongo = mock(MongoClient.class);
        MongoDatabase db = prepareFakeDatabase();
        when(mongo.getDatabase(Mockito.anyString())).thenReturn(db);

        dao.createRequiredUniqueIndex(db.getCollection(TEST_MIGRATION_COLLECTION));

        Document createdIndex = findIndex(db, CHANGE_ID_INDEX_NAME);
        assertNotNull(createdIndex);
        assertTrue(dao.isUnique(createdIndex));
    }


    @Test
    @Ignore("Fongo has not implemented dropIndex for MongoCollection object (issue with mongo driver 3.x)")
    public void shouldDropWrongIndex()
    {
        MongoClient mongo = mock(MongoClient.class);
        MongoDatabase db = prepareFakeDatabase();
        when(mongo.getDatabase(Mockito.anyString())).thenReturn(db);

        MongoCollection<Document> collection = db.getCollection(TEST_MIGRATION_COLLECTION);
        collection.createIndex(new Document()
                .append(MigrationEntry.CHANGE_ID, 1));
        Document index = new Document("name", CHANGE_ID_INDEX_NAME);

        Document createdIndex = findIndex(db, CHANGE_ID_INDEX_NAME);
        assertNotNull(createdIndex);
        assertFalse(dao.isUnique(createdIndex));

        dao.dropIndex(db.getCollection(TEST_MIGRATION_COLLECTION), index);

        assertNull(findIndex(db, CHANGE_ID_INDEX_NAME));
    }


    private Document findIndex(MongoDatabase db, String indexName)
    {
        MongoCursor<Document> iterator = db.getCollection(TEST_MIGRATION_COLLECTION).listIndexes().iterator();
        while (iterator.hasNext())
        {
            Document index = iterator.next();
            String name = (String)index.get("name");
            if (indexName.equals(name))
            {
                return index;
            }
        }
        return null;
    }
}

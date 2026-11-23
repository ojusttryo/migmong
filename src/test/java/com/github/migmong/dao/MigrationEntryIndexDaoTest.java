package com.github.migmong.dao;

import static com.github.migmong.common.Constants.TEST_DB_NAME;
import static com.github.migmong.common.Constants.TEST_MIGRATION_COLLECTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.migmong.AbstractIntegrationTest;
import com.github.migmong.migration.MigrationEntry;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

/**
 * @author lstolowski
 * @since 10.12.14
 */
public class MigrationEntryIndexDaoTest extends AbstractIntegrationTest
{
    private static final String CHANGE_ID_AND_CLASS_INDEX_NAME = "changeId_1_migrationClass_1";

    private ChangeEntryIndexDao dao = new ChangeEntryIndexDao(TEST_MIGRATION_COLLECTION);


    @BeforeEach
    public void cleanDatabase()
    {
        dropDatabase(TEST_DB_NAME);
    }


    @Test
    public void shouldCreateRequiredUniqueIndex()
    {
        try (MongoClient mongo = createClient(TEST_DB_NAME))
        {
            MongoDatabase db = mongo.getDatabase(TEST_DB_NAME);

            dao.createRequiredUniqueIndex(db.getCollection(TEST_MIGRATION_COLLECTION));

            Document createdIndex = findIndex(db, CHANGE_ID_AND_CLASS_INDEX_NAME);
            assertNotNull(createdIndex);
            assertTrue(dao.isUnique(createdIndex));
        }
    }


    @Test
    public void shouldNotRecreateIndexIfItIsAlreadyUnique()
    {
        try (MongoClient mongo = createClient(TEST_DB_NAME))
        {
            MongoDatabase db = mongo.getDatabase(TEST_DB_NAME);
            MongoCollection<Document> collection = db.getCollection(TEST_MIGRATION_COLLECTION);

            dao.createRequiredUniqueIndex(collection);

            MongoCollection<Document> collectionSpy = spy(collection);
            dao.createRequiredUniqueIndex(collectionSpy);

            verify(collectionSpy, never()).createIndex(any(Bson.class), any(IndexOptions.class));
            verify(collectionSpy, never()).dropIndex(anyString());
        }
    }


    @Test
    public void shouldReplaceWrongIndexWithUnique()
    {
        try (MongoClient mongo = createClient(TEST_DB_NAME))
        {
            MongoDatabase db = mongo.getDatabase(TEST_DB_NAME);
            MongoCollection<Document> collection = db.getCollection(TEST_MIGRATION_COLLECTION);

            // non-unique index with only changeId in keys, but with the name of the required unique one
            collection.createIndex(new Document().append(MigrationEntry.CHANGE_ID, 1),
                    new IndexOptions().name(CHANGE_ID_AND_CLASS_INDEX_NAME));

            dao.createRequiredUniqueIndex(collection);

            Document replacedIndex = findIndex(db, CHANGE_ID_AND_CLASS_INDEX_NAME);
            assertNotNull(replacedIndex);
            assertTrue(dao.isUnique(replacedIndex));
            assertEquals(
                    new Document()
                            .append(MigrationEntry.CHANGE_ID, 1)
                            .append(MigrationEntry.MIGRATION_CLASS, 1),
                    replacedIndex.get("key"));
        }
    }


    @Test
    public void shouldDropWrongIndex()
    {
        try (MongoClient mongo = createClient(TEST_DB_NAME))
        {
            MongoDatabase db = mongo.getDatabase(TEST_DB_NAME);
            MongoCollection<Document> collection = db.getCollection(TEST_MIGRATION_COLLECTION);

            // non-unique index with the name of the required unique one
            collection.createIndex(new Document().append(MigrationEntry.CHANGE_ID, 1),
                    new IndexOptions().name(CHANGE_ID_AND_CLASS_INDEX_NAME));
            Document index = new Document("name", CHANGE_ID_AND_CLASS_INDEX_NAME);

            Document createdIndex = findIndex(db, CHANGE_ID_AND_CLASS_INDEX_NAME);
            assertNotNull(createdIndex);
            assertFalse(dao.isUnique(createdIndex));

            dao.dropIndex(collection, index);

            assertNull(findIndex(db, CHANGE_ID_AND_CLASS_INDEX_NAME));
        }
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

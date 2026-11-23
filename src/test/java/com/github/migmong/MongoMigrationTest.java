package com.github.migmong;

import static com.github.migmong.common.Constants.TEST_DB_NAME;
import static com.github.migmong.common.Constants.TEST_LOCK_COLLECTION;
import static com.github.migmong.common.Constants.TEST_MIGRATION_COLLECTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.migmong.dao.ChangeEntryDao;
import com.github.migmong.exception.MigrationConfigurationException;
import com.github.migmong.migration.MigrationEntry;
import com.github.migmong.migrations.V_1__migrations;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

/**
 * Integration tests for the whole migration process, running against a real MongoDB in a container.
 */
public class MongoMigrationTest extends AbstractIntegrationTest
{
    private static final String TEST_MIGRATIONS_PACKAGE = V_1__migrations.class.getPackage().getName();
    private static final String FAILING_MIGRATIONS_PACKAGE = "com.github.migmong.migrationFailing";


    @BeforeEach
    public void cleanDatabase()
    {
        dropDatabase(TEST_DB_NAME);
    }


    @Test
    public void shouldExecuteAllMigrationUnits() throws Exception
    {
        MongoMigration migration = createMigration(TEST_MIGRATIONS_PACKAGE);

        migration.execute();

        try (MongoClient client = createClient(TEST_DB_NAME))
        {
            MongoCollection<Document> migrationLog = client.getDatabase(TEST_DB_NAME)
                    .getCollection(TEST_MIGRATION_COLLECTION);

            assertEquals(9, migrationLog.countDocuments());
            assertEquals(2, migrationLog.countDocuments(new Document(MigrationEntry.CHANGE_ID, 1)));
            assertEquals(2, migrationLog.countDocuments(new Document(MigrationEntry.CHANGE_ID, 2)));
            assertEquals(2, migrationLog.countDocuments(new Document(MigrationEntry.CHANGE_ID, 3)));
            assertEquals(2, migrationLog.countDocuments(new Document(MigrationEntry.CHANGE_ID, 4)));
            assertEquals(0, migrationLog.countDocuments(new Document(MigrationEntry.CHANGE_ID, 5)));
            assertEquals(1, migrationLog.countDocuments(new Document(MigrationEntry.CHANGE_ID, 6)));
        }

        migration.close();
    }


    @Test
    public void shouldExecuteProcessWhenLockAcquired() throws Exception
    {
        MongoMigration migration = createMigration(TEST_MIGRATIONS_PACKAGE);

        migration.execute();

        assertTrue(countDocuments(TEST_MIGRATION_COLLECTION) > 0);

        migration.close();
    }


    @Test
    public void shouldNotExecuteProcessWhenLockNotAcquired() throws Exception
    {
        ChangeEntryDao lockHolder = createConnectedDao();
        assertTrue(lockHolder.acquireProcessLock());

        MongoMigration migration = createMigration(TEST_MIGRATIONS_PACKAGE);
        migration.execute();

        assertEquals(0, countDocuments(TEST_MIGRATION_COLLECTION));
        assertTrue(migration.isExecutionInProgress());

        lockHolder.releaseProcessLock();

        assertFalse(migration.isExecutionInProgress());

        lockHolder.close();
        migration.close();
    }


    @Test
    public void shouldPassOverMigrationUnits() throws Exception
    {
        MongoMigration firstRun = createMigration(TEST_MIGRATIONS_PACKAGE);
        firstRun.execute();
        firstRun.close();

        MongoMigration secondRun = createMigration(TEST_MIGRATIONS_PACKAGE);
        secondRun.execute();

        assertEquals(9, countDocuments(TEST_MIGRATION_COLLECTION));

        secondRun.close();
    }


    @Test
    public void shouldReleaseLockAfterWhenLockAcquired() throws Exception
    {
        MongoMigration migration = createMigration(TEST_MIGRATIONS_PACKAGE);

        migration.execute();

        assertFalse(migration.isExecutionInProgress());
        assertEquals(0, countDocuments(TEST_LOCK_COLLECTION));

        migration.close();
    }


    @Test
    public void shouldReleaseLockWhenExceptionInMigration() throws Exception
    {
        MongoMigration migration = createMigration(FAILING_MIGRATIONS_PACKAGE);

        migration.execute();

        assertEquals(0, countDocuments(TEST_MIGRATION_COLLECTION));
        assertFalse(migration.isExecutionInProgress());

        migration.close();
    }


    @Test
    public void shouldReturnExecutionStatusBasedOnDao() throws Exception
    {
        ChangeEntryDao lockHolder = createConnectedDao();
        assertTrue(lockHolder.acquireProcessLock());

        MongoMigration migration = createMigration(TEST_MIGRATIONS_PACKAGE);
        // cannot acquire the lock here, but the dao gets connected anyway
        migration.execute();

        assertTrue(migration.isExecutionInProgress());

        lockHolder.releaseProcessLock();

        assertFalse(migration.isExecutionInProgress());

        lockHolder.close();
        migration.close();
    }


    @Test
    public void shouldThrowAnExceptionIfNoDbNameSet()
    {
        MongoMigration runner = new MongoMigration(new ConnectionString("mongodb://localhost:27017/"));
        runner.setEnabled(true);
        runner.setMigrationScanPackage(V_1__migrations.class.getPackage().getName());
        assertThrows(MigrationConfigurationException.class, runner::execute);
    }


    private MongoMigration createMigration(String migrationsPackage)
    {
        MongoMigration migration = new MongoMigration(getConnectionString(TEST_DB_NAME));
        migration.setMigrationScanPackage(migrationsPackage);
        migration.setMigrationNamePrefix("V_");
        migration.setMigrationCollectionName(TEST_MIGRATION_COLLECTION);
        migration.setLockCollectionName(TEST_LOCK_COLLECTION);
        return migration;
    }


    private ChangeEntryDao createConnectedDao() throws MigrationConfigurationException
    {
        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, false, 5L, 10L, false);
        dao.connectMongoDb(getConnectionString(TEST_DB_NAME), TEST_DB_NAME);
        return dao;
    }


    private long countDocuments(String collectionName)
    {
        try (MongoClient client = createClient(TEST_DB_NAME))
        {
            return client.getDatabase(TEST_DB_NAME).getCollection(collectionName).countDocuments();
        }
    }
}

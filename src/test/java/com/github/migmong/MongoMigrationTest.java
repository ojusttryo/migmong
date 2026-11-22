package com.github.migmong;

import static com.github.migmong.common.Constants.TEST_DB_NAME;
import static com.github.migmong.common.Constants.TEST_MIGRATION_COLLECTION;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.migmong.dao.ChangeEntryDao;
import com.github.migmong.dao.ChangeEntryIndexDao;
import com.github.migmong.exception.MigrationConfigurationException;
import com.github.migmong.exception.MigrationConnectionException;
import com.github.migmong.migration.MigrationEntry;
import com.github.migmong.migrations.V_1__migrations;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

@ExtendWith(MockitoExtension.class)
public class MongoMigrationTest extends AbstractMigrationTest
{
    @InjectMocks
    private MongoMigration migration = new MongoMigration();

    @Mock
    private ChangeEntryDao dao;

    @Mock
    private ChangeEntryIndexDao indexDao;

    private MongoDatabase fakeMongoDatabase;


    @BeforeEach
    public void prepare() throws MigrationConfigurationException, MigrationConnectionException
    {
        fakeMongoDatabase = prepareFakeDatabase();
        fakeMongoDatabase.getCollection(TEST_MIGRATION_COLLECTION).drop();

        when(dao.connectMongoDb(any(MongoClientURI.class), anyString())).thenReturn(fakeMongoDatabase);
        when(dao.getMongoDatabase()).thenReturn(fakeMongoDatabase);

        doCallRealMethod().when(dao).save(any(MigrationEntry.class));
        doCallRealMethod().when(dao).setMigrationCollectionName(anyString());
        doCallRealMethod().when(dao).setIndexDao(any(ChangeEntryIndexDao.class));

        dao.setIndexDao(indexDao);
        dao.setMigrationCollectionName(TEST_MIGRATION_COLLECTION);

        migration.setDbName(TEST_DB_NAME);
        migration.setEnabled(true);
        migration.setMigrationNamePrefix("V_");
        migration.setMigrationScanPackage(V_1__migrations.class.getPackage().getName());
    }


    @Test
    public void shouldExecuteAllMigrationUnits() throws Exception
    {
        when(dao.acquireProcessLock()).thenReturn(true);
        when(dao.isNewMigrationUnit(any(MigrationEntry.class))).thenReturn(true);

        migration.execute();

        verify(dao, times(9)).save(any(MigrationEntry.class));

        long change1 = fakeMongoDatabase.getCollection(TEST_MIGRATION_COLLECTION)
                .countDocuments(new Document().append(MigrationEntry.CHANGE_ID, 1));
        assertEquals(2, change1);

        long change2 = fakeMongoDatabase.getCollection(TEST_MIGRATION_COLLECTION)
                .countDocuments(new Document().append(MigrationEntry.CHANGE_ID, 2));
        assertEquals(2, change2);

        long change3 = fakeMongoDatabase.getCollection(TEST_MIGRATION_COLLECTION)
                .countDocuments(new Document().append(MigrationEntry.CHANGE_ID, 3));
        assertEquals(2, change3);

        long change4 = fakeMongoDatabase.getCollection(TEST_MIGRATION_COLLECTION)
                .countDocuments(new Document().append(MigrationEntry.CHANGE_ID, 4));
        assertEquals(2, change4);

        long change5 = fakeMongoDatabase.getCollection(TEST_MIGRATION_COLLECTION)
                .countDocuments(new Document().append(MigrationEntry.CHANGE_ID, 5));
        assertEquals(0, change5);

        long change6 = fakeMongoDatabase.getCollection(TEST_MIGRATION_COLLECTION)
                .countDocuments(new Document().append(MigrationEntry.CHANGE_ID, 6));
        assertEquals(1, change6);

        long changeAll = fakeMongoDatabase.getCollection(TEST_MIGRATION_COLLECTION)
                .countDocuments(new Document());
        assertEquals(9, changeAll);
    }


    @Test
    public void shouldExecuteProcessWhenLockAcquired() throws Exception
    {
        when(dao.acquireProcessLock()).thenReturn(true);

        migration.execute();

        verify(dao, atLeastOnce()).isNewMigrationUnit(any(MigrationEntry.class));
    }


    @Test
    public void shouldNotExecuteProcessWhenLockNotAcquired() throws Exception
    {
        when(dao.acquireProcessLock()).thenReturn(false);

        migration.execute();

        verify(dao, never()).isNewMigrationUnit(any(MigrationEntry.class));
    }


    @Test
    public void shouldPassOverMigrationUnits() throws Exception
    {
        when(dao.isNewMigrationUnit(any(MigrationEntry.class))).thenReturn(false);

        migration.execute();

        verify(dao, times(0)).save(any(MigrationEntry.class));
    }


    @Test
    public void shouldReleaseLockAfterWhenLockAcquired() throws Exception
    {
        when(dao.acquireProcessLock()).thenReturn(true);

        migration.execute();

        verify(dao).releaseProcessLock();
    }


    @Test
    public void shouldReleaseLockWhenExceptionInMigration() throws Exception
    {
        // would be nicer with a mock for the whole execution, but this would mean breaking out to separate class..
        // this should be "good enough"
        when(dao.acquireProcessLock()).thenReturn(true);
        when(dao.isNewMigrationUnit(any(MigrationEntry.class))).thenThrow(RuntimeException.class);

        // have to catch the exception to be able to verify after
        try
        {
            migration.execute();
        }
        catch (Exception e)
        {
            // do nothing
        }

        verify(dao).releaseProcessLock();
    }


    @Test
    public void shouldReturnExecutionStatusBasedOnDao() throws Exception
    {
        when(dao.isProccessLockHeld()).thenReturn(true);

        boolean inProgress = migration.isExecutionInProgress();

        assertTrue(inProgress);
    }


    @Test
    public void shouldThrowAnExceptionIfNoDbNameSet()
    {
        MongoMigration runner = new MongoMigration(new MongoClientURI("mongodb://localhost:27017/"));
        runner.setEnabled(true);
        runner.setMigrationScanPackage(V_1__migrations.class.getPackage().getName());
        assertThrows(MigrationConfigurationException.class, runner::execute);
    }
}

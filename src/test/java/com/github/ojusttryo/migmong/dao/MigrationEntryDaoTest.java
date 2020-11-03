package com.github.ojusttryo.migmong.dao;

import static com.github.ojusttryo.migmong.common.Constants.TEST_DB_NAME;
import static com.github.ojusttryo.migmong.common.Constants.TEST_LOCK_COLLECTION;
import static com.github.ojusttryo.migmong.common.Constants.TEST_MIGRATION_COLLECTION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bson.Document;
import org.junit.Test;

import com.github.ojusttryo.migmong.AbstractMigrationTest;
import com.github.ojusttryo.migmong.exception.MigrationConfigurationException;
import com.github.ojusttryo.migmong.exception.MigrationLockException;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 *
 * @author lstolowski
 * @since 10.12.14
 */
public class MigrationEntryDaoTest extends AbstractMigrationTest
{
    private static final boolean WAIT_FOR_LOCK = false;
    private static final long CHANGE_LOG_LOCK_WAIT_TIME = 5L;
    private static final long CHANGE_LOG_LOCK_POLL_RATE = 10L;
    private static final boolean THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK = false;


    @Test
    public void shouldCheckLockHeldFromFromLockDao() throws Exception
    {
        MongoClient mongoClient = mock(MongoClient.class);
        MongoDatabase db = prepareFakeDatabase();
        when(mongoClient.getDatabase(anyString())).thenReturn(db);

        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, WAIT_FOR_LOCK,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);

        LockDao lockDao = mock(LockDao.class);
        dao.setLockDao(lockDao);

        dao.connectMongoDb(mongoClient, TEST_DB_NAME);

        when(lockDao.isLockHeld(db)).thenReturn(true);
        boolean lockHeld = dao.isProccessLockHeld();

        assertTrue(lockHeld);
    }


    @Test
    public void shouldGetLockWhenLockDaoGetsLock() throws Exception
    {
        MongoClient mongoClient = mock(MongoClient.class);
        MongoDatabase db = prepareFakeDatabase();
        when(mongoClient.getDatabase(anyString())).thenReturn(db);

        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, WAIT_FOR_LOCK,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);

        LockDao lockDao = mock(LockDao.class);
        when(lockDao.acquireLock(any(MongoDatabase.class))).thenReturn(true);
        dao.setLockDao(lockDao);

        dao.connectMongoDb(mongoClient, TEST_DB_NAME);

        boolean hasLock = dao.acquireProcessLock();

        assertTrue(hasLock);
    }


    @Test
    public void shouldInitiateLock() throws MigrationConfigurationException
    {
        MongoClient mongoClient = mock(MongoClient.class);
        MongoDatabase db = prepareFakeDatabase();
        when(mongoClient.getDatabase(anyString())).thenReturn(db);

        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, WAIT_FOR_LOCK,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);
        ChangeEntryIndexDao indexDaoMock = mock(ChangeEntryIndexDao.class);
        dao.setIndexDao(indexDaoMock);

        LockDao lockDao = mock(LockDao.class);
        dao.setLockDao(lockDao);

        dao.connectMongoDb(mongoClient, TEST_DB_NAME);

        verify(lockDao).initializeLock(db);
    }


    @Test
    public void shouldNotCreateChangeIdAuthorIndexIfFound() throws MigrationConfigurationException
    {
        MongoClient mongoClient = mock(MongoClient.class);
        MongoDatabase db = prepareFakeDatabase();
        when(mongoClient.getDatabase(anyString())).thenReturn(db);

        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, WAIT_FOR_LOCK,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);
        ChangeEntryIndexDao indexDaoMock = mock(ChangeEntryIndexDao.class);
        when(indexDaoMock.findRequiredIndex(db)).thenReturn(new Document());
        when(indexDaoMock.isUnique(any(Document.class))).thenReturn(true);
        dao.setIndexDao(indexDaoMock);

        dao.connectMongoDb(mongoClient, TEST_DB_NAME);

        verify(indexDaoMock, times(0)).createRequiredUniqueIndex(db.getCollection(TEST_MIGRATION_COLLECTION));
        verify(indexDaoMock, times(0)).dropIndex(db.getCollection(TEST_MIGRATION_COLLECTION), new Document());
    }


    @Test
    public void shouldReleaseLockFromLockDao() throws Exception
    {
        MongoClient mongoClient = mock(MongoClient.class);
        MongoDatabase db = prepareFakeDatabase();
        when(mongoClient.getDatabase(anyString())).thenReturn(db);

        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, WAIT_FOR_LOCK,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);

        LockDao lockDao = mock(LockDao.class);
        dao.setLockDao(lockDao);

        dao.connectMongoDb(mongoClient, TEST_DB_NAME);
        dao.releaseProcessLock();

        verify(lockDao).releaseLock(any(MongoDatabase.class));
    }


    @Test(expected = MigrationLockException.class)
    public void shouldThrowLockExceptionIfThrowExceptionIsTrue() throws Exception
    {
        MongoClient mongoClient = mock(MongoClient.class);
        MongoDatabase db = prepareFakeDatabase();
        when(mongoClient.getDatabase(anyString())).thenReturn(db);

        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, WAIT_FOR_LOCK,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, true);

        LockDao lockDao = mock(LockDao.class);
        when(lockDao.acquireLock(any(MongoDatabase.class))).thenReturn(false);
        dao.setLockDao(lockDao);

        dao.connectMongoDb(mongoClient, TEST_DB_NAME);
        boolean hasLock = dao.acquireProcessLock();

        assertFalse(hasLock);
    }


    @Test
    public void shouldWaitForLockIfWaitForLockIsTrue() throws Exception
    {
        MongoClient mongoClient = mock(MongoClient.class);
        MongoDatabase db = prepareFakeDatabase();
        when(mongoClient.getDatabase(anyString())).thenReturn(db);

        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, true,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);

        LockDao lockDao = mock(LockDao.class);
        when(lockDao.acquireLock(any(MongoDatabase.class))).thenReturn(false, true);
        dao.setLockDao(lockDao);

        dao.connectMongoDb(mongoClient, TEST_DB_NAME);
        boolean hasLock = dao.acquireProcessLock();

        verify(lockDao, times(2)).acquireLock(any(MongoDatabase.class));
        assertTrue(hasLock);
    }

}

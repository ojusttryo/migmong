package com.github.migmong.dao;

import static com.github.migmong.common.Constants.TEST_DB_NAME;
import static com.github.migmong.common.Constants.TEST_LOCK_COLLECTION;
import static com.github.migmong.common.Constants.TEST_MIGRATION_COLLECTION;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.migmong.AbstractIntegrationTest;
import com.github.migmong.exception.MigrationConfigurationException;
import com.github.migmong.exception.MigrationLockException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 *
 * @author lstolowski
 * @since 10.12.14
 */
public class MigrationEntryDaoTest extends AbstractIntegrationTest
{
    private static final boolean WAIT_FOR_LOCK = false;
    private static final long CHANGE_LOG_LOCK_WAIT_TIME = 5L;
    private static final long CHANGE_LOG_LOCK_POLL_RATE = 10L;
    private static final boolean THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK = false;


    @BeforeEach
    public void cleanDatabase()
    {
        dropDatabase(TEST_DB_NAME);
    }


    @Test
    public void shouldCheckLockHeldFromFromLockDao() throws Exception
    {
        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, WAIT_FOR_LOCK,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);

        LockDao lockDao = mock(LockDao.class);
        dao.setLockDao(lockDao);

        try (MongoClient mongoClient = createClient(TEST_DB_NAME))
        {
            dao.connectMongoDb(mongoClient, TEST_DB_NAME);

            when(lockDao.isLockHeld(any(MongoDatabase.class))).thenReturn(true);
            boolean lockHeld = dao.isProccessLockHeld();

            assertTrue(lockHeld);
        }
    }


    @Test
    public void shouldGetLockWhenLockDaoGetsLock() throws Exception
    {
        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, WAIT_FOR_LOCK,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);

        LockDao lockDao = mock(LockDao.class);
        when(lockDao.acquireLock(any(MongoDatabase.class))).thenReturn(true);
        dao.setLockDao(lockDao);

        try (MongoClient mongoClient = createClient(TEST_DB_NAME))
        {
            dao.connectMongoDb(mongoClient, TEST_DB_NAME);

            boolean hasLock = dao.acquireProcessLock();

            assertTrue(hasLock);
        }
    }


    @Test
    public void shouldInitiateLock() throws MigrationConfigurationException
    {
        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, WAIT_FOR_LOCK,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);

        LockDao lockDao = mock(LockDao.class);
        dao.setLockDao(lockDao);

        try (MongoClient mongoClient = createClient(TEST_DB_NAME))
        {
            dao.connectMongoDb(mongoClient, TEST_DB_NAME);

            verify(lockDao).initializeLock(any(MongoDatabase.class));
        }
    }


    @Test
    public void shouldCreateRequiredUniqueIndexOnConnect() throws MigrationConfigurationException
    {
        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, WAIT_FOR_LOCK,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);

        ChangeEntryIndexDao indexDaoMock = mock(ChangeEntryIndexDao.class);
        dao.setIndexDao(indexDaoMock);

        try (MongoClient mongoClient = createClient(TEST_DB_NAME))
        {
            dao.connectMongoDb(mongoClient, TEST_DB_NAME);

            verify(indexDaoMock).createRequiredUniqueIndex(any());
        }
    }


    @Test
    public void shouldReleaseLockFromLockDao() throws Exception
    {
        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, WAIT_FOR_LOCK,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);

        LockDao lockDao = mock(LockDao.class);
        dao.setLockDao(lockDao);

        try (MongoClient mongoClient = createClient(TEST_DB_NAME))
        {
            dao.connectMongoDb(mongoClient, TEST_DB_NAME);
            dao.releaseProcessLock();

            verify(lockDao).releaseLock(any(MongoDatabase.class));
        }
    }


    @Test
    public void shouldThrowLockExceptionIfThrowExceptionIsTrue() throws Exception
    {
        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, WAIT_FOR_LOCK,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, true);

        LockDao lockDao = mock(LockDao.class);
        when(lockDao.acquireLock(any(MongoDatabase.class))).thenReturn(false);
        dao.setLockDao(lockDao);

        try (MongoClient mongoClient = createClient(TEST_DB_NAME))
        {
            dao.connectMongoDb(mongoClient, TEST_DB_NAME);

            assertThrows(MigrationLockException.class, () -> dao.acquireProcessLock());
        }
    }


    @Test
    public void shouldWaitForLockIfWaitForLockIsTrue() throws Exception
    {
        ChangeEntryDao dao = new ChangeEntryDao(TEST_MIGRATION_COLLECTION, TEST_LOCK_COLLECTION, true,
                CHANGE_LOG_LOCK_WAIT_TIME, CHANGE_LOG_LOCK_POLL_RATE, THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);

        LockDao lockDao = mock(LockDao.class);
        when(lockDao.acquireLock(any(MongoDatabase.class))).thenReturn(false, true);
        dao.setLockDao(lockDao);

        try (MongoClient mongoClient = createClient(TEST_DB_NAME))
        {
            dao.connectMongoDb(mongoClient, TEST_DB_NAME);
            boolean hasLock = dao.acquireProcessLock();

            verify(lockDao, times(2)).acquireLock(any(MongoDatabase.class));
            assertTrue(hasLock);
        }
    }

}

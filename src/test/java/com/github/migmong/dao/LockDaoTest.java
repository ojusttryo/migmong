package com.github.migmong.dao;

import static com.github.migmong.common.Constants.TEST_DB_NAME;
import static com.github.migmong.common.Constants.TEST_LOCK_COLLECTION;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.migmong.AbstractIntegrationTest;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * Tests for acquiring and releasing locks
 * @author colsson11
 * @since 13.01.15
 */
public class LockDaoTest extends AbstractIntegrationTest
{
    @BeforeEach
    public void cleanDatabase()
    {
        dropDatabase(TEST_DB_NAME);
    }


    @Test
    public void releaseLockShouldBeIdempotent()
    {
        try (MongoClient client = createClient(TEST_DB_NAME))
        {
            MongoDatabase db = client.getDatabase(TEST_DB_NAME);
            LockDao dao = new LockDao(TEST_LOCK_COLLECTION);
            dao.initializeLock(db);

            dao.releaseLock(db);
            dao.releaseLock(db);
            boolean hasLock = dao.acquireLock(db);

            assertTrue(hasLock);
        }
    }


    @Test
    public void shouldGetLockWhenNotPreviouslyHeld()
    {
        try (MongoClient client = createClient(TEST_DB_NAME))
        {
            MongoDatabase db = client.getDatabase(TEST_DB_NAME);
            LockDao dao = new LockDao(TEST_LOCK_COLLECTION);
            dao.initializeLock(db);

            boolean hasLock = dao.acquireLock(db);

            assertTrue(hasLock);
        }
    }


    @Test
    public void shouldGetLockWhenPreviouslyHeldAndReleased()
    {
        try (MongoClient client = createClient(TEST_DB_NAME))
        {
            MongoDatabase db = client.getDatabase(TEST_DB_NAME);
            LockDao dao = new LockDao(TEST_LOCK_COLLECTION);
            dao.initializeLock(db);

            dao.acquireLock(db);
            dao.releaseLock(db);
            boolean hasLock = dao.acquireLock(db);

            assertTrue(hasLock);
        }
    }


    @Test
    public void shouldNotGetLockWhenPreviouslyHeld()
    {
        try (MongoClient client = createClient(TEST_DB_NAME))
        {
            MongoDatabase db = client.getDatabase(TEST_DB_NAME);
            LockDao dao = new LockDao(TEST_LOCK_COLLECTION);
            dao.initializeLock(db);

            dao.acquireLock(db);
            boolean hasLock = dao.acquireLock(db);

            assertFalse(hasLock);
        }
    }


    @Test
    public void whenLockHeldCheckReturnsTrue()
    {
        try (MongoClient client = createClient(TEST_DB_NAME))
        {
            MongoDatabase db = client.getDatabase(TEST_DB_NAME);
            LockDao dao = new LockDao(TEST_LOCK_COLLECTION);
            dao.initializeLock(db);

            dao.acquireLock(db);

            assertTrue(dao.isLockHeld(db));
        }
    }


    @Test
    public void whenLockNotHeldCheckReturnsFalse()
    {
        try (MongoClient client = createClient(TEST_DB_NAME))
        {
            MongoDatabase db = client.getDatabase(TEST_DB_NAME);
            LockDao dao = new LockDao(TEST_LOCK_COLLECTION);
            dao.initializeLock(db);

            assertFalse(dao.isLockHeld(db));
        }
    }

}

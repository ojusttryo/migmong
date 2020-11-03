package com.github.ojusttryo.migmong.dao;

import static com.github.ojusttryo.migmong.common.Constants.TEST_LOCK_COLLECTION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.ojusttryo.migmong.AbstractMigrationTest;
import com.mongodb.client.MongoDatabase;

/**
 * Tests for acquiring and releasing locks
 * @author colsson11
 * @since 13.01.15
 */
public class LockDaoTest extends AbstractMigrationTest
{
    @Test
    public void releaseLockShouldBeIdempotent()
    {
        MongoDatabase db = prepareFakeDatabase();
        LockDao dao = new LockDao(TEST_LOCK_COLLECTION);
        dao.initializeLock(db);

        dao.releaseLock(db);
        dao.releaseLock(db);
        boolean hasLock = dao.acquireLock(db);

        assertTrue(hasLock);
    }


    @Test
    public void shouldGetLockWhenNotPreviouslyHeld()
    {
        MongoDatabase db = prepareFakeDatabase();
        LockDao dao = new LockDao(TEST_LOCK_COLLECTION);
        dao.initializeLock(db);

        boolean hasLock = dao.acquireLock(db);

        assertTrue(hasLock);
    }


    @Test
    public void shouldGetLockWhenPreviouslyHeldAndReleased()
    {
        MongoDatabase db = prepareFakeDatabase();
        LockDao dao = new LockDao(TEST_LOCK_COLLECTION);
        dao.initializeLock(db);

        dao.acquireLock(db);
        dao.releaseLock(db);
        boolean hasLock = dao.acquireLock(db);

        assertTrue(hasLock);
    }


    @Test
    public void shouldNotGetLockWhenPreviouslyHeld()
    {
        MongoDatabase db = prepareFakeDatabase();
        LockDao dao = new LockDao(TEST_LOCK_COLLECTION);
        dao.initializeLock(db);

        dao.acquireLock(db);
        boolean hasLock = dao.acquireLock(db);

        assertFalse(hasLock);
    }


    @Test
    public void whenLockHeldCheckReturnsTrue()
    {
        MongoDatabase db = prepareFakeDatabase();
        LockDao dao = new LockDao(TEST_LOCK_COLLECTION);
        dao.initializeLock(db);

        dao.acquireLock(db);

        assertTrue(dao.isLockHeld(db));
    }


    @Test
    public void whenLockNotHeldCheckReturnsFalse()
    {
        MongoDatabase db = prepareFakeDatabase();
        LockDao dao = new LockDao(TEST_LOCK_COLLECTION);
        dao.initializeLock(db);

        assertFalse(dao.isLockHeld(db));
    }



}

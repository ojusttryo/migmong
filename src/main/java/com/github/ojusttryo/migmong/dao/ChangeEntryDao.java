package com.github.ojusttryo.migmong.dao;

import static org.springframework.util.StringUtils.hasText;

import java.util.Date;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ojusttryo.migmong.exception.MigrationLockException;
import com.github.ojusttryo.migmong.exception.MigrationConfigurationException;
import com.github.ojusttryo.migmong.exception.MigrationConnectionException;
import com.github.ojusttryo.migmong.migration.MigrationEntry;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * @author lstolowski
 * @since 27/07/2014
 */
public class ChangeEntryDao
{
    private static final Logger logger = LoggerFactory.getLogger(ChangeEntryDao.class);

    private MongoDatabase mongoDatabase;
    private MongoClient mongoClient;
    private ChangeEntryIndexDao indexDao;
    private String migrationCollectionName;
    private boolean waitForLock;
    private long changeLogLockWaitTime;
    private long changeLogLockPollRate;
    private boolean throwExceptionIfCannotObtainLock;

    private LockDao lockDao;


    public ChangeEntryDao(String migrationCollectionName, String lockCollectionName, boolean waitForLock,
            long changeLogLockWaitTime, long changeLogLockPollRate, boolean throwExceptionIfCannotObtainLock)
    {
        this.indexDao = new ChangeEntryIndexDao(migrationCollectionName);
        this.lockDao = new LockDao(lockCollectionName);
        this.migrationCollectionName = migrationCollectionName;
        this.waitForLock = waitForLock;
        this.changeLogLockWaitTime = changeLogLockWaitTime;
        this.changeLogLockPollRate = changeLogLockPollRate;
        this.throwExceptionIfCannotObtainLock = throwExceptionIfCannotObtainLock;
    }


    /**
     * Try to acquire process lock
     *
     * @return true if successfully acquired, false otherwise
     * @throws MigrationConnectionException exception
     * @throws MigrationLockException exception
     */
    public boolean acquireProcessLock() throws MigrationConnectionException, MigrationLockException
    {
        verifyDbConnection();
        boolean acquired = lockDao.acquireLock(getMongoDatabase());

        if (!acquired && waitForLock)
        {
            long timeToGiveUp = new Date().getTime() + (changeLogLockWaitTime * 1000 * 60);
            while (!acquired && new Date().getTime() < timeToGiveUp)
            {
                acquired = lockDao.acquireLock(getMongoDatabase());
                if (!acquired)
                {
                    logger.info("Waiting for changelog lock....");
                    try
                    {
                        Thread.sleep(changeLogLockPollRate * 1000);
                    }
                    catch (InterruptedException e)
                    {
                        // nothing
                    }
                }
            }
        }

        if (!acquired && throwExceptionIfCannotObtainLock)
        {
            logger.info("MongoMigration did not acquire process lock. Throwing exception.");
            throw new MigrationLockException("Could not acquire process lock");
        }

        return acquired;
    }


    public void close()
    {
        this.mongoClient.close();
    }


    public MongoDatabase connectMongoDb(MongoClient mongo, String dbName) throws MigrationConfigurationException
    {
        if (!hasText(dbName))
            throw new MigrationConfigurationException("Database name is not set");

        this.mongoClient = mongo;
        mongoDatabase = mongo.getDatabase(dbName);

        ensureChangeLogCollectionIndex(mongoDatabase.getCollection(migrationCollectionName));
        initializeLock();
        return mongoDatabase;
    }


    public MongoDatabase connectMongoDb(MongoClientURI mongoClientURI, String dbName)
            throws MigrationConfigurationException
    {

        final MongoClient mongoClient = new MongoClient(mongoClientURI);
        final String database = (!hasText(dbName)) ? mongoClientURI.getDatabase() : dbName;

        return this.connectMongoDb(mongoClient, database);
    }


    public long getChangeLogLockPollRate()
    {
        return changeLogLockPollRate;
    }


    public long getChangeLogLockWaitTime()
    {
        return changeLogLockWaitTime;
    }


    public MongoDatabase getMongoDatabase()
    {
        return mongoDatabase;
    }


    public boolean isNewMigrationUnit(MigrationEntry migrationEntry) throws MigrationConnectionException
    {
        verifyDbConnection();

        MongoCollection<Document> mongobeeChangeLog = getMongoDatabase().getCollection(migrationCollectionName);
        Document entry = mongobeeChangeLog.find(migrationEntry.buildSearchQueryDBObject()).first();

        return entry == null;
    }


    public boolean isProccessLockHeld() throws MigrationConnectionException
    {
        verifyDbConnection();
        return lockDao.isLockHeld(getMongoDatabase());
    }


    public boolean isThrowExceptionIfCannotObtainLock()
    {
        return throwExceptionIfCannotObtainLock;
    }


    public boolean isWaitForLock()
    {
        return waitForLock;
    }


    public void releaseProcessLock() throws MigrationConnectionException
    {
        verifyDbConnection();
        lockDao.releaseLock(getMongoDatabase());
    }


    public void save(MigrationEntry migrationEntry) throws MigrationConnectionException
    {
        verifyDbConnection();

        MongoCollection<Document> migMong = getMongoDatabase().getCollection(migrationCollectionName);
        migMong.insertOne(migrationEntry.buildFullDBObject());
    }


    public void setChangeLogLockPollRate(long changeLogLockPollRate)
    {
        this.changeLogLockPollRate = changeLogLockPollRate;
    }


    public void setChangeLogLockWaitTime(long changeLogLockWaitTime)
    {
        this.changeLogLockWaitTime = changeLogLockWaitTime;
    }


    public void setMigrationCollectionName(String migrationCollectionName)
    {
        this.indexDao.setChangelogCollectionName(migrationCollectionName);
        this.migrationCollectionName = migrationCollectionName;
    }


    public void setIndexDao(ChangeEntryIndexDao changeEntryIndexDao)
    {
        this.indexDao = changeEntryIndexDao;
    }


    public void setLockCollectionName(String lockCollectionName)
    {
        this.lockDao.setLockCollectionName(lockCollectionName);
    }


    public void setThrowExceptionIfCannotObtainLock(boolean throwExceptionIfCannotObtainLock)
    {
        this.throwExceptionIfCannotObtainLock = throwExceptionIfCannotObtainLock;
    }


    public void setWaitForLock(boolean waitForLock)
    {
        this.waitForLock = waitForLock;
    }


    /* Visible for testing */
    void setLockDao(LockDao lockDao)
    {
        this.lockDao = lockDao;
    }


    private void ensureChangeLogCollectionIndex(MongoCollection<Document> collection)
    {
        Document index = indexDao.findRequiredChangeAndAuthorIndex(mongoDatabase);
        if (index == null)
        {
            indexDao.createRequiredUniqueIndex(collection);
            logger.debug("Index in collection " + migrationCollectionName + " was created");
        }
        else if (!indexDao.isUnique(index))
        {
            indexDao.dropIndex(collection, index);
            indexDao.createRequiredUniqueIndex(collection);
            logger.debug("Index in collection " + migrationCollectionName + " was recreated");
        }

    }


    private void initializeLock()
    {
        lockDao.initializeLock(mongoDatabase);
    }


    private void verifyDbConnection() throws MigrationConnectionException
    {
        if (getMongoDatabase() == null)
        {
            throw new MigrationConnectionException("Database is not connected. MongoMigration has thrown an unexpected error",
                    new NullPointerException());
        }
    }

}

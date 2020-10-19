package com.github.ojusttryo.migmong.dao;

import static org.springframework.util.StringUtils.hasText;

import java.util.Date;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ojusttryo.migmong.exception.MigMongLockException;
import com.github.ojusttryo.migmong.changeset.ChangeEntry;
import com.github.ojusttryo.migmong.exception.MigMongConfigurationException;
import com.github.ojusttryo.migmong.exception.MigMongConnectionException;
import com.mongodb.DB;
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
    private String changelogCollectionName;
    private boolean waitForLock;
    private long changeLogLockWaitTime;
    private long changeLogLockPollRate;
    private boolean throwExceptionIfCannotObtainLock;

    private LockDao lockDao;


    public ChangeEntryDao(String changelogCollectionName, String lockCollectionName, boolean waitForLock,
            long changeLogLockWaitTime, long changeLogLockPollRate, boolean throwExceptionIfCannotObtainLock)
    {
        this.indexDao = new ChangeEntryIndexDao(changelogCollectionName);
        this.lockDao = new LockDao(lockCollectionName);
        this.changelogCollectionName = changelogCollectionName;
        this.waitForLock = waitForLock;
        this.changeLogLockWaitTime = changeLogLockWaitTime;
        this.changeLogLockPollRate = changeLogLockPollRate;
        this.throwExceptionIfCannotObtainLock = throwExceptionIfCannotObtainLock;
    }


    /**
     * Try to acquire process lock
     *
     * @return true if successfully acquired, false otherwise
     * @throws MigMongConnectionException exception
     * @throws MigMongLockException exception
     */
    public boolean acquireProcessLock() throws MigMongConnectionException, MigMongLockException
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
            logger.info("MigMong did not acquire process lock. Throwing exception.");
            throw new MigMongLockException("Could not acquire process lock");
        }

        return acquired;
    }


    public void close()
    {
        this.mongoClient.close();
    }


    public MongoDatabase connectMongoDb(MongoClient mongo, String dbName) throws MigMongConfigurationException
    {
        if (!hasText(dbName))
        {
            throw new MigMongConfigurationException(
                    "DB name is not set. Should be defined in MongoDB URI or via setter");
        }
        else
        {

            this.mongoClient = mongo;
            mongoDatabase = mongo.getDatabase(dbName);

            ensureChangeLogCollectionIndex(mongoDatabase.getCollection(changelogCollectionName));
            initializeLock();
            return mongoDatabase;
        }
    }


    public MongoDatabase connectMongoDb(MongoClientURI mongoClientURI, String dbName)
            throws MigMongConfigurationException
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


    public boolean isNewChange(ChangeEntry changeEntry) throws MigMongConnectionException
    {
        verifyDbConnection();

        MongoCollection<Document> mongobeeChangeLog = getMongoDatabase().getCollection(changelogCollectionName);
        Document entry = mongobeeChangeLog.find(changeEntry.buildSearchQueryDBObject()).first();

        return entry == null;
    }


    public boolean isProccessLockHeld() throws MigMongConnectionException
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


    public void releaseProcessLock() throws MigMongConnectionException
    {
        verifyDbConnection();
        lockDao.releaseLock(getMongoDatabase());
    }


    public void save(ChangeEntry changeEntry) throws MigMongConnectionException
    {
        verifyDbConnection();

        MongoCollection<Document> migMong = getMongoDatabase().getCollection(changelogCollectionName);
        migMong.insertOne(changeEntry.buildFullDBObject());
    }


    public void setChangeLogLockPollRate(long changeLogLockPollRate)
    {
        this.changeLogLockPollRate = changeLogLockPollRate;
    }


    public void setChangeLogLockWaitTime(long changeLogLockWaitTime)
    {
        this.changeLogLockWaitTime = changeLogLockWaitTime;
    }


    public void setChangelogCollectionName(String changelogCollectionName)
    {
        this.indexDao.setChangelogCollectionName(changelogCollectionName);
        this.changelogCollectionName = changelogCollectionName;
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
            logger.debug("Index in collection " + changelogCollectionName + " was created");
        }
        else if (!indexDao.isUnique(index))
        {
            indexDao.dropIndex(collection, index);
            indexDao.createRequiredUniqueIndex(collection);
            logger.debug("Index in collection " + changelogCollectionName + " was recreated");
        }

    }


    private void initializeLock()
    {
        lockDao.initializeLock(mongoDatabase);
    }


    private void verifyDbConnection() throws MigMongConnectionException
    {
        if (getMongoDatabase() == null)
        {
            throw new MigMongConnectionException("Database is not connected. MigMong has thrown an unexpected error",
                    new NullPointerException());
        }
    }

}

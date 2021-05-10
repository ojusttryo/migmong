package com.github.migmong.dao;

import static org.springframework.util.StringUtils.hasText;

import java.util.Date;

import org.bson.Document;

import com.github.migmong.exception.MigrationConfigurationException;
import com.github.migmong.exception.MigrationConnectionException;
import com.github.migmong.exception.MigrationLockException;
import com.github.migmong.migration.MigrationEntry;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import lombok.extern.slf4j.Slf4j;

/**
 * @author lstolowski
 * @since 27/07/2014
 */
@Slf4j
public class ChangeEntryDao
{
    private MongoDatabase mongoDatabase;
    private MongoClient mongoClient;
    private ChangeEntryIndexDao indexDao;
    private String migrationCollectionName;
    private boolean waitForLock;
    private long migrationLockWaitTime;
    private long migrationLockPollRate;
    private boolean throwExceptionIfCannotObtainLock;

    private LockDao lockDao;


    public ChangeEntryDao(String migrationCollectionName, String lockCollectionName, boolean waitForLock,
            long migrationLockWaitTime, long migrationLockPollRate, boolean throwExceptionIfCannotObtainLock)
    {
        this.indexDao = new ChangeEntryIndexDao(migrationCollectionName);
        this.lockDao = new LockDao(lockCollectionName);
        this.migrationCollectionName = migrationCollectionName;
        this.waitForLock = waitForLock;
        this.migrationLockWaitTime = migrationLockWaitTime;
        this.migrationLockPollRate = migrationLockPollRate;
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
            long timeToGiveUp = new Date().getTime() + (migrationLockWaitTime * 1000 * 60);
            while (!acquired && new Date().getTime() < timeToGiveUp)
            {
                acquired = lockDao.acquireLock(getMongoDatabase());
                if (!acquired)
                {
                    log.info("Waiting for migration lock....");
                    try
                    {
                        Thread.sleep(migrationLockPollRate * 1000);
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
            log.info("MongoMigration did not acquire process lock. Throwing exception.");
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
        indexDao.createRequiredUniqueIndex(mongoDatabase.getCollection(migrationCollectionName));
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


    public long getMigrationLockPollRate()
    {
        return migrationLockPollRate;
    }


    public long getMigrationLockWaitTime()
    {
        return migrationLockWaitTime;
    }


    public MongoDatabase getMongoDatabase()
    {
        return mongoDatabase;
    }


    public boolean isNewMigrationUnit(MigrationEntry migrationEntry) throws MigrationConnectionException
    {
        verifyDbConnection();

        MongoCollection<Document> migrationCollection = getMongoDatabase().getCollection(migrationCollectionName);
        Document entry = migrationCollection.find(migrationEntry.buildSearchQueryDBObject()).first();

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


    public void setMigrationLockPollRate(long migrationLockPollRate)
    {
        this.migrationLockPollRate = migrationLockPollRate;
    }


    public void setMigrationLockWaitTime(long migrationLockWaitTime)
    {
        this.migrationLockWaitTime = migrationLockWaitTime;
    }


    public void setMigrationCollectionName(String migrationCollectionName)
    {
        this.indexDao.setMigrationCollection(migrationCollectionName);
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

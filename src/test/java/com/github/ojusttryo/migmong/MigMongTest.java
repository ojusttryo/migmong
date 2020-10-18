package com.github.ojusttryo.migmong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.Collections;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.github.fakemongo.Fongo;
import com.github.ojusttryo.migmong.test.changelogs.MongobeeTestResource;
import com.github.ojusttryo.migmong.changeset.ChangeEntry;
import com.github.ojusttryo.migmong.dao.ChangeEntryDao;
import com.github.ojusttryo.migmong.dao.ChangeEntryIndexDao;
import com.github.ojusttryo.migmong.exception.MigMongConfigurationException;
import com.github.ojusttryo.migmong.exception.MigMongException;
import com.mongodb.DB;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

@RunWith(MockitoJUnitRunner.class)
public class MigMongTest
{

    private static final String CHANGELOG_COLLECTION_NAME = "dbchangelog";
    @InjectMocks
    private MigMong runner = new MigMong();

    @Mock
    private ChangeEntryDao dao;

    @Mock
    private ChangeEntryIndexDao indexDao;

    private DB fakeDb;
    private MongoDatabase fakeMongoDatabase;


    @After
    public void cleanUp()
    {
        runner.setMongoTemplate(null);
        fakeDb.dropDatabase();
    }


    @Before
    public void init() throws MigMongException, UnknownHostException
    {
        fakeDb = new Fongo("testServer").getDB("mongobeetest");
        fakeMongoDatabase = new Fongo("testServer").getDatabase("mongobeetest");
        when(dao.connectMongoDb(any(MongoClientURI.class), anyString()))
                .thenReturn(fakeMongoDatabase);
        when(dao.getDb()).thenReturn(fakeDb);
        when(dao.getMongoDatabase()).thenReturn(fakeMongoDatabase);
        doCallRealMethod().when(dao).save(any(ChangeEntry.class));
        doCallRealMethod().when(dao).setChangelogCollectionName(anyString());
        doCallRealMethod().when(dao).setIndexDao(any(ChangeEntryIndexDao.class));
        dao.setIndexDao(indexDao);
        dao.setChangelogCollectionName(CHANGELOG_COLLECTION_NAME);

        runner.setDbName("mongobeetest");
        runner.setEnabled(true);
        runner.setChangeLogsScanPackage(MongobeeTestResource.class.getPackage().getName());
    }


    @Test
    public void shouldExecuteAllChangeSets() throws Exception
    {
        // given
        when(dao.acquireProcessLock()).thenReturn(true);
        when(dao.isNewChange(any(ChangeEntry.class))).thenReturn(true);

        // when
        runner.execute();

        // then
        verify(dao, times(13)).save(any(ChangeEntry.class)); // 13 changesets saved to dbchangelog

        // dbchangelog collection checking
        long change1 = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME).count(new Document()
                .append(ChangeEntry.CHANGE_ID, "test1")
                .append(ChangeEntry.AUTHOR, "testuser"));
        assertEquals(1, change1);
        long change2 = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME).count(new Document()
                .append(ChangeEntry.CHANGE_ID, "test2")
                .append(ChangeEntry.AUTHOR, "testuser"));
        assertEquals(1, change2);
        long change3 = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME).count(new Document()
                .append(ChangeEntry.CHANGE_ID, "test3")
                .append(ChangeEntry.AUTHOR, "testuser"));
        assertEquals(1, change3);
        long change4 = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME).count(new Document()
                .append(ChangeEntry.CHANGE_ID, "test4")
                .append(ChangeEntry.AUTHOR, "testuser"));
        assertEquals(1, change4);
        long change5 = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME).count(new Document()
                .append(ChangeEntry.CHANGE_ID, "test5")
                .append(ChangeEntry.AUTHOR, "testuser"));
        assertEquals(1, change5);

        long changeAll = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME).count(new Document()
                .append(ChangeEntry.AUTHOR, "testuser"));
        assertEquals(12, changeAll);
    }


    @Test
    public void shouldExecuteProcessWhenLockAcquired() throws Exception
    {
        // given
        when(dao.acquireProcessLock()).thenReturn(true);

        // when
        runner.execute();

        // then
        verify(dao, atLeastOnce()).isNewChange(any(ChangeEntry.class));
    }


    @Test
    public void shouldNotExecuteProcessWhenLockNotAcquired() throws Exception
    {
        // given
        when(dao.acquireProcessLock()).thenReturn(false);

        // when
        runner.execute();

        // then
        verify(dao, never()).isNewChange(any(ChangeEntry.class));
    }


    @Test
    public void shouldPassOverChangeSets() throws Exception
    {
        // given
        when(dao.isNewChange(any(ChangeEntry.class))).thenReturn(false);

        // when
        runner.execute();

        // then
        verify(dao, times(0)).save(any(ChangeEntry.class)); // no changesets saved to dbchangelog
    }


    @Test
    public void shouldReleaseLockAfterWhenLockAcquired() throws Exception
    {
        // given
        when(dao.acquireProcessLock()).thenReturn(true);

        // when
        runner.execute();

        // then
        verify(dao).releaseProcessLock();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void shouldReleaseLockWhenExceptionInMigration() throws Exception
    {

        // given
        // would be nicer with a mock for the whole execution, but this would mean breaking out to separate class..
        // this should be "good enough"
        when(dao.acquireProcessLock()).thenReturn(true);
        when(dao.isNewChange(any(ChangeEntry.class))).thenThrow(RuntimeException.class);

        // when
        // have to catch the exception to be able to verify after
        try
        {
            runner.execute();
        }
        catch (Exception e)
        {
            // do nothing
        }
        // then
        verify(dao).releaseProcessLock();

    }


    @Test
    public void shouldReturnExecutionStatusBasedOnDao() throws Exception
    {
        // given
        when(dao.isProccessLockHeld()).thenReturn(true);

        boolean inProgress = runner.isExecutionInProgress();

        // then
        assertTrue(inProgress);
    }


    @Test(expected = MigMongConfigurationException.class)
    public void shouldThrowAnExceptionIfNoDbNameSet() throws Exception
    {
        MigMong runner = new MigMong(new MongoClientURI("mongodb://localhost:27017/"));
        runner.setEnabled(true);
        runner.setChangeLogsScanPackage(MongobeeTestResource.class.getPackage().getName());
        runner.execute();
    }


    @Test
    public void shouldUsePreConfiguredMongoTemplate() throws Exception
    {
        MongoTemplate mt = mock(MongoTemplate.class);
        when(mt.getCollectionNames()).thenReturn(Collections.EMPTY_SET);
        when(dao.acquireProcessLock()).thenReturn(true);
        when(dao.isNewChange(any(ChangeEntry.class))).thenReturn(true);
        runner.setMongoTemplate(mt);
        runner.afterPropertiesSet();
        verify(mt).getCollectionNames();
    }

}

package com.github.ojusttryo.migmong;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.fakemongo.Fongo;
import com.github.ojusttryo.migmong.resources.EnvironmentMock;
import com.github.ojusttryo.migmong.test.changelogs.AnotherMongobeeTestResource;
import com.github.ojusttryo.migmong.test.profiles.def.UnProfiledChangeLog;
import com.github.ojusttryo.migmong.migration.MigrationEntry;
import com.github.ojusttryo.migmong.dao.ChangeEntryDao;
import com.github.ojusttryo.migmong.dao.ChangeEntryIndexDao;
import com.mongodb.DB;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

/**
 * Tests for Spring profiles integration
 *
 * @author lstolowski
 * @since 2014-09-17
 */
@RunWith(MockitoJUnitRunner.class)
public class MongoMigrationProfileTest
{
    public static final int CHANGELOG_COUNT = 13;
    private static final String CHANGELOG_COLLECTION_NAME = "dbchangelog";
    @InjectMocks
    private MongoMigration runner = new MongoMigration();

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
    public void init() throws Exception
    {
        fakeDb = new Fongo("testServer").getDB("mongobeetest");
        fakeMongoDatabase = new Fongo("testServer").getDatabase("mongobeetest");

        when(dao.connectMongoDb(any(MongoClientURI.class), anyString()))
                .thenReturn(fakeMongoDatabase);
        when(dao.getMongoDatabase()).thenReturn(fakeMongoDatabase);
        when(dao.acquireProcessLock()).thenReturn(true);
        doCallRealMethod().when(dao).save(any(MigrationEntry.class));
        doCallRealMethod().when(dao).setMigrationCollectionName(anyString());
        doCallRealMethod().when(dao).setIndexDao(any(ChangeEntryIndexDao.class));
        dao.setIndexDao(indexDao);
        dao.setMigrationCollectionName(CHANGELOG_COLLECTION_NAME);

        runner.setDbName("mongobeetest");
        runner.setEnabled(true);
    } // TODO code duplication



    @Test
    public void shouldRunAllChangeSets() throws Exception
    {
        // given
        runner.setSpringEnvironment(new EnvironmentMock("dev"));
        runner.setMigrationScanPackage(AnotherMongobeeTestResource.class.getPackage().getName());
        when(dao.isNewMigrationUnit(any(MigrationEntry.class))).thenReturn(true);

        // when
        runner.execute();

        // then
        long changes = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME).countDocuments(new Document());
        assertEquals(CHANGELOG_COUNT, changes);
    }


    @Test
    public void shouldRunChangeSetsWhenEmptyEnv() throws Exception
    {
        // given
        runner.setSpringEnvironment(new EnvironmentMock());
        runner.setMigrationScanPackage(AnotherMongobeeTestResource.class.getPackage().getName());
        when(dao.isNewMigrationUnit(any(MigrationEntry.class))).thenReturn(true);

        // when
        runner.execute();

        // then
        long changes = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME).countDocuments(new Document());
        assertEquals(CHANGELOG_COUNT, changes);
    }


    @Test
    public void shouldRunChangeSetsWhenNoEnv() throws Exception
    {
        // given
        runner.setSpringEnvironment(null);
        runner.setMigrationScanPackage(AnotherMongobeeTestResource.class.getPackage().getName());
        when(dao.isNewMigrationUnit(any(MigrationEntry.class))).thenReturn(true);

        // when
        runner.execute();

        // then
        long changes = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME).countDocuments(new Document());
        assertEquals(CHANGELOG_COUNT, changes);
    }


    @Test
    public void shouldRunUnprofiledChangeLog() throws Exception
    {
        // given
        runner.setSpringEnvironment(new EnvironmentMock("test"));
        runner.setMigrationScanPackage(UnProfiledChangeLog.class.getPackage().getName());
        when(dao.isNewMigrationUnit(any(MigrationEntry.class))).thenReturn(true);

        // when
        runner.execute();

        // then
        long change1 = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME)
                .countDocuments(new Document()
                        .append(MigrationEntry.CHANGE_ID, 1));
        assertEquals(1, change1);

        long change2 = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME)
                .countDocuments(new Document()
                        .append(MigrationEntry.CHANGE_ID, 2));
        assertEquals(1, change2);

        long change3 = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME)
                .countDocuments(new Document()
                        .append(MigrationEntry.CHANGE_ID, 3));
        assertEquals(1, change3);  //  @Profile("dev")  should not match

        long change4 = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME)
                .countDocuments(new Document()
                        .append(MigrationEntry.CHANGE_ID, 4));
        assertEquals(0, change4);  //  @Profile("pro")  should not match

        long change5 = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME)
                .countDocuments(new Document()
                        .append(MigrationEntry.CHANGE_ID, 5));
        assertEquals(1, change5);  //  @Profile("!pro")  should match
    }

}

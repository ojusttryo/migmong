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
import com.github.ojusttryo.migmong.test.changelogs.EnvironmentDependentTestResource;
import com.github.ojusttryo.migmong.changeset.ChangeEntry;
import com.github.ojusttryo.migmong.dao.ChangeEntryDao;
import com.github.ojusttryo.migmong.dao.ChangeEntryIndexDao;
import com.mongodb.DB;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

/**
 * Created by lstolowski on 13.07.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class MigMongEnvTest
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
    public void init() throws Exception
    {
        fakeDb = new Fongo("testServer").getDB("mongobeetest");
        fakeMongoDatabase = new Fongo("testServer").getDatabase("mongobeetest");

        when(dao.connectMongoDb(any(MongoClientURI.class), anyString()))
                .thenReturn(fakeMongoDatabase);
        when(dao.getDb()).thenReturn(fakeDb);
        when(dao.getMongoDatabase()).thenReturn(fakeMongoDatabase);
        when(dao.acquireProcessLock()).thenReturn(true);
        doCallRealMethod().when(dao).save(any(ChangeEntry.class));
        doCallRealMethod().when(dao).setChangelogCollectionName(anyString());
        doCallRealMethod().when(dao).setIndexDao(any(ChangeEntryIndexDao.class));
        dao.setIndexDao(indexDao);
        dao.setChangelogCollectionName(CHANGELOG_COLLECTION_NAME);

        runner.setDbName("mongobeetest");
        runner.setEnabled(true);
    }  // TODO code duplication


    @Test
    public void shouldRunChangesetWithEnvironment() throws Exception
    {
        // given
        runner.setSpringEnvironment(new EnvironmentMock());
        runner.setChangeLogsScanPackage(EnvironmentDependentTestResource.class.getPackage().getName());
        when(dao.isNewChange(any(ChangeEntry.class))).thenReturn(true);

        // when
        runner.execute();

        // then
        long change1 = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME)
                .count(new Document()
                        .append(ChangeEntry.CHANGE_ID, "Envtest1")
                        .append(ChangeEntry.AUTHOR, "testuser"));
        assertEquals(1, change1);

    }


    @Test
    public void shouldRunChangesetWithNullEnvironment() throws Exception
    {
        // given
        runner.setSpringEnvironment(null);
        runner.setChangeLogsScanPackage(EnvironmentDependentTestResource.class.getPackage().getName());
        when(dao.isNewChange(any(ChangeEntry.class))).thenReturn(true);

        // when
        runner.execute();

        // then
        long change1 = fakeMongoDatabase.getCollection(CHANGELOG_COLLECTION_NAME)
                .count(new Document()
                        .append(ChangeEntry.CHANGE_ID, "Envtest1")
                        .append(ChangeEntry.AUTHOR, "testuser"));
        assertEquals(1, change1);

    }

}

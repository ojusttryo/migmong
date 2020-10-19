package com.github.ojusttryo.migmong.test.changelogs;


import com.github.ojusttryo.migmong.changeset.ChangeLog;
import com.github.ojusttryo.migmong.changeset.ChangeSet;
import com.mongodb.DB;
import com.mongodb.client.MongoDatabase;

/**
 * @author lstolowski
 * @since 30.07.14
 */
@ChangeLog
public class AnotherMongobeeTestResource
{

    @ChangeSet(id = 1)
    public void testChangeSet()
    {
        System.out.println("invoked B1");
    }


    @ChangeSet(id = 2)
    public void testChangeSet2()
    {
        System.out.println("invoked B2");
    }


    @ChangeSet(id = 3)
    public void testChangeSet3(DB db)
    {
        System.out.println("invoked B3 with db=" + db.toString());
    }


    @ChangeSet(id = 6)
    public void testChangeSet6(MongoDatabase mongoDatabase)
    {
        System.out.println("invoked B6 with db=" + mongoDatabase.toString());
    }
}

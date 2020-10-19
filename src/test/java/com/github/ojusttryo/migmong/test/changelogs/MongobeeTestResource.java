package com.github.ojusttryo.migmong.test.changelogs;


import com.github.ojusttryo.migmong.changeset.ChangeLog;
import com.github.ojusttryo.migmong.changeset.ChangeSet;
import com.mongodb.DB;
import com.mongodb.client.MongoDatabase;

/**
 * @author lstolowski
 * @since 27/07/2014
 */
@ChangeLog
public class MongobeeTestResource
{

    @ChangeSet(id = 1)
    public void testChangeSet()
    {
        System.out.println("invoked 1");
    }


    @ChangeSet(id = 2)
    public void testChangeSet2()
    {
        System.out.println("invoked 2");
    }


    @ChangeSet(id = 3)
    public void testChangeSet3(DB db)
    {
        System.out.println("invoked 3 with db=" + db.toString());
    }


    @ChangeSet(id = 4)
    public void testChangeSet5(MongoDatabase mongoDatabase)
    {
        System.out.println("invoked 5 with mongoDatabase=" + mongoDatabase.toString());
    }

}

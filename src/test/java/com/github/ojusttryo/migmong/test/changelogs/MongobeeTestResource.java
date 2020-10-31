package com.github.ojusttryo.migmong.test.changelogs;


import com.github.ojusttryo.migmong.migration.annotations.Migration;
import com.github.ojusttryo.migmong.migration.annotations.MigrationUnit;
import com.mongodb.DB;
import com.mongodb.client.MongoDatabase;

/**
 * @author lstolowski
 * @since 27/07/2014
 */
@Migration
public class MongobeeTestResource
{

    @MigrationUnit(id = 1)
    public void testChangeSet()
    {
        System.out.println("invoked 1");
    }


    @MigrationUnit(id = 2)
    public void testChangeSet2()
    {
        System.out.println("invoked 2");
    }


    @MigrationUnit(id = 3)
    public void testChangeSet3(DB db)
    {
        System.out.println("invoked 3 with db=" + db.toString());
    }


    @MigrationUnit(id = 4)
    public void testChangeSet5(MongoDatabase mongoDatabase)
    {
        System.out.println("invoked 5 with mongoDatabase=" + mongoDatabase.toString());
    }

}

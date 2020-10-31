package com.github.ojusttryo.migmong.test.changelogs;


import com.github.ojusttryo.migmong.migration.annotations.Migration;
import com.github.ojusttryo.migmong.migration.annotations.MigrationUnit;
import com.mongodb.DB;
import com.mongodb.client.MongoDatabase;

/**
 * @author lstolowski
 * @since 30.07.14
 */
@Migration
public class AnotherMongobeeTestResource
{

    @MigrationUnit(id = 1)
    public void testChangeSet()
    {
        System.out.println("invoked B1");
    }


    @MigrationUnit(id = 2)
    public void testChangeSet2()
    {
        System.out.println("invoked B2");
    }


    @MigrationUnit(id = 3)
    public void testChangeSet3(DB db)
    {
        System.out.println("invoked B3 with db=" + db.toString());
    }


    @MigrationUnit(id = 6)
    public void testChangeSet6(MongoDatabase mongoDatabase)
    {
        System.out.println("invoked B6 with db=" + mongoDatabase.toString());
    }
}

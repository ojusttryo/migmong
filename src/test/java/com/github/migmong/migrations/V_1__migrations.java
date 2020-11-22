package com.github.migmong.migrations;


import com.github.migmong.migration.MigrationContext;
import com.github.migmong.migration.annotations.Migration;
import com.github.migmong.migration.annotations.MigrationUnit;

/**
 *
 * @author lstolowski
 * @since 27/07/2014
 */
@Migration
public class V_1__migrations
{
    @MigrationUnit(id = 1)
    public void testMigration1(MigrationContext context)
    {
        System.out.println("invoked 1");
    }


    @MigrationUnit(id = 2)
    public void testMigration2(MigrationContext context)
    {
        System.out.println("invoked 2");
    }


    @MigrationUnit(id = 3)
    public void testMigration3(MigrationContext context)
    {
        System.out.println("invoked 3");
    }


    @MigrationUnit(id = 4)
    public void testMigration4(MigrationContext context)
    {
        System.out.println("invoked 4");
    }
}

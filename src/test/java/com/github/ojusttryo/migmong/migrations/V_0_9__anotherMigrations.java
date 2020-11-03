package com.github.ojusttryo.migmong.migrations;


import com.github.ojusttryo.migmong.migration.MigrationContext;
import com.github.ojusttryo.migmong.migration.annotations.Migration;
import com.github.ojusttryo.migmong.migration.annotations.MigrationUnit;

/**
 *
 * @author lstolowski
 * @since 30.07.14
 */
@Migration
public class V_0_9__anotherMigrations
{
    @MigrationUnit(id = 1)
    public void testMigration1(MigrationContext context)
    {
        System.out.println("invoked B1");
    }


    @MigrationUnit(id = 2)
    public void testMigration2(MigrationContext context)
    {
        System.out.println("invoked B2");
    }


    @MigrationUnit(id = 6, runAlways = true)
    public void testMigration3_withAlways(MigrationContext context)
    {
        System.out.println("invoked B6");
    }


    @MigrationUnit(id = 3, runAlways = true)
    public void testMigration6_withAlways(MigrationContext context)
    {
        System.out.println("invoked B3");
    }
}

package com.github.migmong.migrationWithDuplicates;

import com.github.migmong.migration.annotations.Migration;
import com.github.migmong.migration.annotations.MigrationUnit;

@Migration
public class V_0_1_5__withDuplicates
{
    @MigrationUnit(id = 1)
    public void testMigration()
    {
        System.out.println("invoked B1");
    }


    @MigrationUnit(id = 2)
    public void testMigration2()
    {
        System.out.println("invoked B2");
    }


    @MigrationUnit(id = 2)
    public void testMigration3()
    {
        System.out.println("invoked B3");
    }
}

package com.github.ojusttryo.migmong.utils;

import com.github.ojusttryo.migmong.migration.annotations.Migration;
import com.github.ojusttryo.migmong.migration.annotations.MigrationUnit;

@Migration
public class ChangeLogWithDuplicate
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
    public void testChangeSet3()
    {
        System.out.println("invoked B3");
    }
}

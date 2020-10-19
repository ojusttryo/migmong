package com.github.ojusttryo.migmong.utils;

import com.github.ojusttryo.migmong.changeset.ChangeLog;
import com.github.ojusttryo.migmong.changeset.ChangeSet;

@ChangeLog
public class ChangeLogWithDuplicate
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
    public void testChangeSet3()
    {
        System.out.println("invoked B3");
    }
}

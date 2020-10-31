package com.github.ojusttryo.migmong.test.profiles.def;

import org.springframework.context.annotation.Profile;

import com.github.ojusttryo.migmong.migration.annotations.Migration;
import com.github.ojusttryo.migmong.migration.annotations.MigrationUnit;

/**
 * @author lstolowski
 * @since 2014-09-17
 */
@Migration
public class UnProfiledChangeLog
{
    @MigrationUnit(id = 1)
    public void testChangeSet()
    {
        System.out.println("invoked Pdev1");
    }


    @MigrationUnit(id = 2)
    public void testChangeSet2()
    {
        System.out.println("invoked Pdev2");
    }


    @MigrationUnit(id = 3)
    public void testChangeSet3()
    {
        System.out.println("invoked Pdev3");
    }


    @MigrationUnit(id = 4)
    @Profile("pro")
    public void testChangeSet4()
    {
        System.out.println("invoked Pdev4");
    }


    @MigrationUnit(id = 5)
    @Profile("!pro")
    public void testChangeSet5()
    {
        System.out.println("invoked Pdev5");
    }
}

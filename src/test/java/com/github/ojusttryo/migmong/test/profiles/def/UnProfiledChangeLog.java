package com.github.ojusttryo.migmong.test.profiles.def;

import org.springframework.context.annotation.Profile;

import com.github.ojusttryo.migmong.changeset.ChangeLog;
import com.github.ojusttryo.migmong.changeset.ChangeSet;

/**
 * @author lstolowski
 * @since 2014-09-17
 */
@ChangeLog
public class UnProfiledChangeLog
{
    @ChangeSet(id = 1)
    public void testChangeSet()
    {
        System.out.println("invoked Pdev1");
    }


    @ChangeSet(id = 2)
    public void testChangeSet2()
    {
        System.out.println("invoked Pdev2");
    }


    @ChangeSet(id = 3)
    public void testChangeSet3()
    {
        System.out.println("invoked Pdev3");
    }


    @ChangeSet(id = 4)
    @Profile("pro")
    public void testChangeSet4()
    {
        System.out.println("invoked Pdev4");
    }


    @ChangeSet(id = 5)
    @Profile("!pro")
    public void testChangeSet5()
    {
        System.out.println("invoked Pdev5");
    }
}

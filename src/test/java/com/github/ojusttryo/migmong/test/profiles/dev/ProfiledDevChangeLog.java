package com.github.ojusttryo.migmong.test.profiles.dev;

import org.springframework.context.annotation.Profile;

import com.github.ojusttryo.migmong.changeset.ChangeLog;
import com.github.ojusttryo.migmong.changeset.ChangeSet;

/**
 * @author lstolowski
 * @since 2014-09-17
 */
@ChangeLog(order = "01")
@Profile(value = "dev")
public class ProfiledDevChangeLog
{

    @ChangeSet(author = "testuser", id = "Pdev1", order = "01")
    public void testChangeSet()
    {
        System.out.println("invoked Pdev1");
    }


    @ChangeSet(author = "testuser", id = "Pdev2", order = "02")
    @Profile("pro")
    public void testChangeSet2()
    {
        System.out.println("invoked Pdev2");
    }


    @ChangeSet(author = "testuser", id = "Pdev3", order = "03")
    @Profile("default")
    public void testChangeSet3()
    {
        System.out.println("invoked Pdev3");
    }


    @ChangeSet(author = "testuser", id = "Pdev4", order = "04")
    @Profile("dev")
    public void testChangeSet4()
    {
        System.out.println("invoked Pdev4");
    }

}

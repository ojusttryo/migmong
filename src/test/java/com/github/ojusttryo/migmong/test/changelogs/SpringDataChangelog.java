package com.github.ojusttryo.migmong.test.changelogs;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.github.ojusttryo.migmong.changeset.ChangeLog;
import com.github.ojusttryo.migmong.changeset.ChangeSet;

/**
 * @author abelski
 */
@ChangeLog
public class SpringDataChangelog
{
    @ChangeSet(author = "abelski", id = "spring_test4", order = "04")
    public void testChangeSet(MongoTemplate mongoTemplate)
    {
        System.out.println("invoked  with mongoTemplate=" + mongoTemplate.toString());
        System.out.println("invoked  with mongoTemplate=" + mongoTemplate.getCollectionNames());
    }
}

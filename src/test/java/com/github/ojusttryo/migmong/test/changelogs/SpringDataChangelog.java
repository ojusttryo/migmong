package com.github.ojusttryo.migmong.test.changelogs;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.github.ojusttryo.migmong.migration.annotations.Migration;
import com.github.ojusttryo.migmong.migration.annotations.MigrationUnit;

/**
 * @author abelski
 */
@Migration
public class SpringDataChangelog
{
    @MigrationUnit(id = 4)
    public void testChangeSet(MongoTemplate mongoTemplate)
    {
        System.out.println("invoked  with mongoTemplate=" + mongoTemplate.toString());
        System.out.println("invoked  with mongoTemplate=" + mongoTemplate.getCollectionNames());
    }
}

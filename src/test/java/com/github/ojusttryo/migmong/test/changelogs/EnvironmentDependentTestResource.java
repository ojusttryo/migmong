package com.github.ojusttryo.migmong.test.changelogs;

import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.github.ojusttryo.migmong.changeset.ChangeLog;
import com.github.ojusttryo.migmong.changeset.ChangeSet;

@ChangeLog
public class EnvironmentDependentTestResource
{
    @ChangeSet(id = 1)
    public void testChangeSet7WithEnvironment(MongoTemplate template, Environment env)
    {
        System.out.println("invoked Envtest1 with mongotemplate=" + template.toString() + " and Environment " + env);
    }
}

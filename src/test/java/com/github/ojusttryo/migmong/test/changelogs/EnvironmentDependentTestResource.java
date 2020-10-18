package com.github.ojusttryo.migmong.test.changelogs;

import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.github.ojusttryo.migmong.changeset.ChangeLog;
import com.github.ojusttryo.migmong.changeset.ChangeSet;

@ChangeLog(order = "3")
public class EnvironmentDependentTestResource
{
    @ChangeSet(author = "testuser", id = "Envtest1", order = "01")
    public void testChangeSet7WithEnvironment(MongoTemplate template, Environment env)
    {
        System.out.println("invoked Envtest1 with mongotemplate=" + template.toString() + " and Environment " + env);
    }
}

package com.github.ojusttryo.migmong.test.changelogs;

import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.github.ojusttryo.migmong.migration.annotations.Migration;
import com.github.ojusttryo.migmong.migration.annotations.MigrationUnit;

@Migration
public class EnvironmentDependentTestResource
{
    @MigrationUnit(id = 1)
    public void testChangeSet7WithEnvironment(MongoTemplate template, Environment env)
    {
        System.out.println("invoked Envtest1 with mongotemplate=" + template.toString() + " and Environment " + env);
    }
}

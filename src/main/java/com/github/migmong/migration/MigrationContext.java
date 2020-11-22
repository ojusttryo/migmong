package com.github.migmong.migration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoDatabase;

/**
 * MigrationInfo context to be passed to every migration unit
 */
public class MigrationContext
{
    private Environment springEnvironment;
    private ApplicationContext applicationContext;
    private MongoTemplate mongoTemplate;
    private MongoDatabase mongoDatabase;
    private Map<String, Object> customVariables = new HashMap<>();


    public Environment getSpringEnvironment()
    {
        return springEnvironment;
    }


    public void setSpringEnvironment(Environment springEnvironment)
    {
        this.springEnvironment = springEnvironment;
    }


    public ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }


    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }


    public MongoTemplate getMongoTemplate()
    {
        return mongoTemplate;
    }


    public void setMongoTemplate(MongoTemplate mongoTemplate)
    {
        this.mongoTemplate = mongoTemplate;
    }


    public MongoDatabase getMongoDatabase()
    {
        return mongoDatabase;
    }


    public void setMongoDatabase(MongoDatabase mongoDatabase)
    {
        this.mongoDatabase = mongoDatabase;
    }


    public void setVariable(String name, Object variable)
    {
        customVariables.put(name, variable);
    }


    public Object getVariable(String name)
    {
        return customVariables.get(name);
    }
}

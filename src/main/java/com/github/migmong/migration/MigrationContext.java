package com.github.migmong.migration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoDatabase;

import lombok.Data;

/**
 * MigrationInfo context to be passed to every migration unit
 */
@Data
public class MigrationContext
{
    private Environment springEnvironment;
    private ApplicationContext applicationContext;
    private MongoTemplate mongoTemplate;
    private MongoDatabase mongoDatabase;
    private Map<String, Object> customVariables = new HashMap<>();


    public void setVariable(String name, Object variable)
    {
        customVariables.put(name, variable);
    }

    public Object getVariable(String name)
    {
        return customVariables.get(name);
    }
}

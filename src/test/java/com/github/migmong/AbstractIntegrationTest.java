package com.github.migmong;

import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * Base class for tests running against a real MongoDB in a container.
 * The container is started once and shared between all test classes in the JVM.
 */
public abstract class AbstractIntegrationTest
{
    protected static final MongoDBContainer MONGO_CONTAINER = MongoContainer.getInstance();

    static
    {
        MONGO_CONTAINER.start();
    }


    protected static ConnectionString getConnectionString(String dbName)
    {
        return new ConnectionString(MONGO_CONTAINER.getReplicaSetUrl(dbName));
    }


    protected static MongoClient createClient(String dbName)
    {
        return MongoClients.create(getConnectionString(dbName));
    }


    protected static void dropDatabase(String dbName)
    {
        try (MongoClient client = createClient(dbName))
        {
            client.getDatabase(dbName).drop();
        }
    }

}

package com.github.migmong;

import org.testcontainers.containers.MongoDBContainer;

/**
 * Singleton MongoDB container shared by all tests in the JVM.
 */
public class MongoContainer extends MongoDBContainer
{
    private static final String IMAGE_VERSION = "mongo:7.0";

    private static MongoContainer container;


    private MongoContainer()
    {
        super(IMAGE_VERSION);
    }


    public static MongoContainer getInstance()
    {
        if (container == null)
        {
            container = new MongoContainer();
        }
        return container;
    }


    @Override
    public void stop()
    {
        // do nothing, JVM handles shut down
    }

}

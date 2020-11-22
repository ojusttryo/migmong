package com.github.migmong;

import static com.github.migmong.common.Constants.TEST_DB_NAME;

import java.net.InetSocketAddress;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

public abstract class AbstractMigrationTest
{
    protected MongoDatabase prepareFakeDatabase()
    {
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        InetSocketAddress serverAddress = mongoServer.bind();
        MongoClient client = new MongoClient(new ServerAddress(serverAddress));
        MongoDatabase db = client.getDatabase(TEST_DB_NAME);
        return db;
    }
}

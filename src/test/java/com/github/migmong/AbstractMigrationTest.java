package com.github.migmong;

import static com.github.migmong.common.Constants.TEST_DB_NAME;

import java.net.InetSocketAddress;
import java.util.List;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

public abstract class AbstractMigrationTest
{
    protected MongoDatabase prepareFakeDatabase()
    {
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        InetSocketAddress serverAddress = mongoServer.bind();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.hosts(List.of(new ServerAddress(serverAddress))))
                .build();
        MongoClient client = MongoClients.create(settings);
        MongoDatabase db = client.getDatabase(TEST_DB_NAME);
        return db;
    }
}

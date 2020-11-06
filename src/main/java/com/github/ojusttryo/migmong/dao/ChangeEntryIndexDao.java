package com.github.ojusttryo.migmong.dao;

import org.bson.Document;

import com.github.ojusttryo.migmong.migration.MigrationEntry;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;

/**
 * @author lstolowski
 * @since 10.12.14
 */
public class ChangeEntryIndexDao
{

    private String migrationCollection;


    public ChangeEntryIndexDao(String migrationCollection)
    {
        this.migrationCollection = migrationCollection;
    }


    public void createRequiredUniqueIndex(MongoCollection<Document> collection)
    {
        collection.createIndex(new Document()
                        .append(MigrationEntry.CHANGE_ID, 1)
                        .append(MigrationEntry.MIGRATION_CLASS, 1),
                new IndexOptions().unique(true));
    }


    public void dropIndex(MongoCollection<Document> collection, Document index)
    {
        collection.dropIndex(index.get("name").toString());
    }


    public boolean isUnique(Document index)
    {
        Object unique = index.get("unique");
        return (unique != null && unique instanceof Boolean && (Boolean)unique);
    }


    public void setMigrationCollection(String migrationCollection)
    {
        this.migrationCollection = migrationCollection;
    }

}

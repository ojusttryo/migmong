package com.github.ojusttryo.migmong.dao;

import org.bson.Document;

import com.github.ojusttryo.migmong.migration.MigrationEntry;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

/**
 * @author lstolowski
 * @since 10.12.14
 */
public class ChangeEntryIndexDao
{

    private String changelogCollectionName;


    public ChangeEntryIndexDao(String changelogCollectionName)
    {
        this.changelogCollectionName = changelogCollectionName;
    }


    public void createRequiredUniqueIndex(MongoCollection<Document> collection)
    {
        collection.createIndex(new Document().append(MigrationEntry.CHANGE_ID, 1), new IndexOptions().unique(true));
    }


    public void dropIndex(MongoCollection<Document> collection, Document index)
    {
        collection.dropIndex(index.get("name").toString());
    }


    public Document findRequiredChangeAndAuthorIndex(MongoDatabase db)
    {
        MongoCollection<Document> indexes = db.getCollection("system.indexes");
        Document index = indexes
                .find(new Document()
                    .append("ns", db.getName() + "." + changelogCollectionName)
                    .append("key", new Document().append(MigrationEntry.CHANGE_ID, 1)))
                .first();

        return index;
    }


    public boolean isUnique(Document index)
    {
        Object unique = index.get("unique");
        return (unique != null && unique instanceof Boolean && (Boolean)unique);
    }


    public void setChangelogCollectionName(String changelogCollectionName)
    {
        this.changelogCollectionName = changelogCollectionName;
    }

}

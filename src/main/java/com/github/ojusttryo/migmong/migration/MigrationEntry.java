package com.github.ojusttryo.migmong.migration;

import java.util.Date;

import org.bson.Document;


/**
 * Entry in the changes collection log
 * Type: entity class.
 *
 * @author lstolowski
 * @since 27/07/2014
 */
public class MigrationEntry
{
    public static final String CHANGE_ID = "changeId";
    public static final String TIMESTAMP = "timestamp";
    public static final String MIGRATION_CLASS = "migrationClass";
    public static final String MIGRATION_UNIT = "migrationUnit";

    private int changeId;
    private Date timestamp;
    private String migrationClass;
    private String migrationUnit;


    public MigrationEntry(int changeId, Date timestamp, String migrationClass, String migrationUnit)
    {
        this.changeId = changeId;
        this.timestamp = new Date(timestamp.getTime());
        this.migrationClass = migrationClass;
        this.migrationUnit = migrationUnit;
    }


    public Document buildFullDBObject()
    {
        Document entry = new Document();

        entry.append(CHANGE_ID, this.changeId)
                .append(TIMESTAMP, this.timestamp)
                .append(MIGRATION_CLASS, this.migrationClass)
                .append(MIGRATION_UNIT, this.migrationUnit);

        return entry;
    }


    public Document buildSearchQueryDBObject()
    {
        return new Document()
                .append(CHANGE_ID, this.changeId)
                .append(MIGRATION_CLASS, this.migrationClass);
    }


    public int getChangeId()
    {
        return this.changeId;
    }


    public String getMigrationClass()
    {
        return this.migrationClass;
    }


    public String getMigrationUnit()
    {
        return this.migrationUnit;
    }


    public Date getTimestamp()
    {
        return this.timestamp;
    }


    @Override
    public String toString()
    {
        return "[MigrationUnit: id=" + this.changeId +
                ", migrationClass=" + this.migrationClass +
                ", migrationUnit=" + this.migrationUnit + "]";
    }

}

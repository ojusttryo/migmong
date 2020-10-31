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
    public static final String CHANGE_LOG_CLASS = "changeLogClass";
    public static final String CHANGE_SET_METHOD = "changeSetMethod";

    private int changeId;
    private Date timestamp;
    private String changeLogClass;
    private String changeSetMethodName;


    public MigrationEntry(int changeId, Date timestamp, String changeLogClass, String changeSetMethodName)
    {
        this.changeId = changeId;
        this.timestamp = new Date(timestamp.getTime());
        this.changeLogClass = changeLogClass;
        this.changeSetMethodName = changeSetMethodName;
    }


    public Document buildFullDBObject()
    {
        Document entry = new Document();

        entry.append(CHANGE_ID, this.changeId)
                .append(TIMESTAMP, this.timestamp)
                .append(CHANGE_LOG_CLASS, this.changeLogClass)
                .append(CHANGE_SET_METHOD, this.changeSetMethodName);

        return entry;
    }


    public Document buildSearchQueryDBObject()
    {
        return new Document()
                .append(CHANGE_ID, this.changeId)
                .append(CHANGE_LOG_CLASS, this.changeLogClass);
    }


    public int getChangeId()
    {
        return this.changeId;
    }


    public String getChangeLogClass()
    {
        return this.changeLogClass;
    }


    public String getChangeSetMethodName()
    {
        return this.changeSetMethodName;
    }


    public Date getTimestamp()
    {
        return this.timestamp;
    }


    @Override
    public String toString()
    {
        return "[MigrationUnit: id=" + this.changeId +
                ", changeLogClass=" + this.changeLogClass +
                ", changeSetMethod=" + this.changeSetMethodName + "]";
    }

}

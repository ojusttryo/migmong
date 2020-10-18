package com.github.ojusttryo.migmong.changeset;

import java.util.Date;

import org.bson.Document;


/**
 * Entry in the changes collection log
 * Type: entity class.
 *
 * @author lstolowski
 * @since 27/07/2014
 */
public class ChangeEntry
{
    public static final String CHANGE_ID = "changeId";
    public static final String AUTHOR = "author";
    public static final String TIMESTAMP = "timestamp";
    public static final String CHANGE_LOG_CLASS = "changeLogClass";
    public static final String CHANGE_SET_METHOD = "changeSetMethod";

    private String changeId;
    private String author;
    private Date timestamp;
    private String changeLogClass;
    private String changeSetMethodName;


    public ChangeEntry(String changeId, String author, Date timestamp, String changeLogClass,
            String changeSetMethodName)
    {
        this.changeId = changeId;
        this.author = author;
        this.timestamp = new Date(timestamp.getTime());
        this.changeLogClass = changeLogClass;
        this.changeSetMethodName = changeSetMethodName;
    }


    public Document buildFullDBObject()
    {
        Document entry = new Document();

        entry.append(CHANGE_ID, this.changeId)
                .append(AUTHOR, this.author)
                .append(TIMESTAMP, this.timestamp)
                .append(CHANGE_LOG_CLASS, this.changeLogClass)
                .append(CHANGE_SET_METHOD, this.changeSetMethodName);

        return entry;
    }


    public Document buildSearchQueryDBObject()
    {
        return new Document()
                .append(CHANGE_ID, this.changeId)
                .append(AUTHOR, this.author);
    }


    public String getAuthor()
    {
        return this.author;
    }


    public String getChangeId()
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
        return "[ChangeSet: id=" + this.changeId +
                ", author=" + this.author +
                ", changeLogClass=" + this.changeLogClass +
                ", changeSetMethod=" + this.changeSetMethodName + "]";
    }

}

package com.github.migmong.migration;

import java.util.Date;

import org.bson.Document;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Entry in the changes collection log
 * Type: entity class.
 *
 * @author lstolowski
 * @since 27/07/2014
 */
@Data
@RequiredArgsConstructor
public class MigrationEntry
{
    public static final String CHANGE_ID = "changeId";
    public static final String TIMESTAMP = "timestamp";
    public static final String MIGRATION_CLASS = "migrationClass";
    public static final String MIGRATION_UNIT = "migrationUnit";

    private final int changeId;
    private final Date timestamp;
    private final String migrationClass;
    private final String migrationUnit;


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


    @Override
    public String toString()
    {
        return "[MigrationUnit: id=" + this.changeId +
                ", migrationClass=" + this.migrationClass +
                ", migrationUnit=" + this.migrationUnit + "]";
    }

}

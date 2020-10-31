package com.github.ojusttryo.migmong.utils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;

import com.github.ojusttryo.migmong.migration.annotations.MigrationUnit;

/**
 * Sort {@link MigrationUnit} by 'order' value
 *
 * @author lstolowski
 * @since 2014-09-17
 */
public class ChangeSetComparator implements Comparator<Method>, Serializable
{
    @Override
    public int compare(Method left, Method right)
    {
        MigrationUnit leftMigrationUnit = left.getAnnotation(MigrationUnit.class);
        MigrationUnit rightMigrationUnit = right.getAnnotation(MigrationUnit.class);
        return (leftMigrationUnit.id() - rightMigrationUnit.id());
    }
}

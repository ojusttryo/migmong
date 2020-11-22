package com.github.migmong.utils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;

import com.github.migmong.migration.annotations.MigrationUnit;

/**
 * Sort {@link MigrationUnit} by 'id' value
 */
public class MigrationUnitComparator implements Comparator<Method>, Serializable
{
    @Override
    public int compare(Method left, Method right)
    {
        MigrationUnit leftMigrationUnit = left.getAnnotation(MigrationUnit.class);
        MigrationUnit rightMigrationUnit = right.getAnnotation(MigrationUnit.class);
        return (leftMigrationUnit.id() - rightMigrationUnit.id());
    }
}

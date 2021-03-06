package com.github.migmong.utils;

import static java.util.Arrays.asList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.github.migmong.exception.MigrationUnitException;
import com.github.migmong.migration.MigrationInfo;
import com.github.migmong.migration.annotations.Migration;
import com.github.migmong.migration.annotations.MigrationUnit;
import com.github.migmong.exception.MigrationException;
import com.github.migmong.migration.MigrationEntry;

import lombok.RequiredArgsConstructor;

/**
 * Utilities to deal with reflections and annotations
 *
 * @author lstolowski
 * @since 27/07/2014
 */
@RequiredArgsConstructor
public class MigrationService
{
    private final String migrationsBasePackage;


    public MigrationEntry createMigrationEntry(Method migrationUnit)
    {
        if (migrationUnit.isAnnotationPresent(MigrationUnit.class))
        {
            return new MigrationEntry(
                    migrationUnit.getAnnotation(MigrationUnit.class).id(),
                    new Date(),
                    migrationUnit.getDeclaringClass().getSimpleName(),
                    migrationUnit.getName());
        }
        else
        {
            return null;
        }
    }


    public List<MigrationInfo> fetchMigrations(String prefix) throws MigrationException
    {
        Reflections reflections = new Reflections(migrationsBasePackage);
        List<MigrationInfo> migrations = new ArrayList<>();
        List<Class<?>> migrationClasses = new ArrayList<>(reflections.getTypesAnnotatedWith(Migration.class));
        for (Class<?> migrationClass : migrationClasses)
        {
            // When we search for migrations in a package, reflection returns not only classes from it,
            // but also from similar ones. E.g. if we set package name as 'x.y.migrations', reflection will have also
            // found classes from 'x.y.migrationsFromAnotherPackage'
            boolean hasRightPackage = migrationClass.getPackageName().contentEquals(migrationsBasePackage);
            boolean hasRightPrefix = migrationClass.getSimpleName().startsWith(prefix);
            if (!hasRightPrefix || !hasRightPackage)
                continue;

            MigrationInfo migrationInfo = new MigrationInfo(migrationClass, prefix);
            migrations.add(migrationInfo);
        }
        migrations.sort(new MigrationComparator());
        return migrations;
    }


    public List<Method> fetchMigrationUnits(final Class<?> type) throws MigrationUnitException
    {
        final List<Method> migrationUnits = filterMigrationUnitAnnotation(asList(type.getDeclaredMethods()));
        Collections.sort(migrationUnits, new MigrationUnitComparator());
        return migrationUnits;
    }


    public boolean isAlwaysRunnableMigration(Method migrationUnitMethod)
    {
        if (!migrationUnitMethod.isAnnotationPresent(MigrationUnit.class))
            return false;

        MigrationUnit annotation = migrationUnitMethod.getAnnotation(MigrationUnit.class);
        return annotation.runAlways();
    }


    private List<Method> filterMigrationUnitAnnotation(List<Method> allMethods) throws MigrationUnitException
    {
        final Set<Integer> migrationUnitIds = new HashSet<>();
        final List<Method> migrationUnitMethods = new ArrayList<>();
        for (final Method method : allMethods)
        {
            if (method.isAnnotationPresent(MigrationUnit.class))
            {
                int id = method.getAnnotation(MigrationUnit.class).id();
                if (migrationUnitIds.contains(id))
                    throw new MigrationUnitException(String.format("Duplicated MigrationUnit id found: '%s'", id));

                migrationUnitIds.add(id);
                migrationUnitMethods.add(method);
            }
        }
        return migrationUnitMethods;
    }
}

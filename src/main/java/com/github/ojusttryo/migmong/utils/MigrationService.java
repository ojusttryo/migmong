package com.github.ojusttryo.migmong.utils;

import static java.util.Arrays.asList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.springframework.core.env.Environment;

import com.github.ojusttryo.migmong.exception.MigrationUnitException;
import com.github.ojusttryo.migmong.migration.Version;
import com.github.ojusttryo.migmong.migration.annotations.Migration;
import com.github.ojusttryo.migmong.migration.annotations.MigrationUnit;
import com.github.ojusttryo.migmong.migration.MigrationInfo;
import com.github.ojusttryo.migmong.migration.MigrationEntry;
import com.github.ojusttryo.migmong.exception.MigrationException;

/**
 * Utilities to deal with reflections and annotations
 *
 * @author lstolowski
 * @since 27/07/2014
 */
public class MigrationService
{
    private static final String DEFAULT_PROFILE = "default";

    private final String changeLogsBasePackage;
    private final List<String> activeProfiles;


    public MigrationService(String changeLogsBasePackage)
    {
        this(changeLogsBasePackage, null);
    }


    public MigrationService(String changeLogsBasePackage, Environment environment)
    {
        this.changeLogsBasePackage = changeLogsBasePackage;

        if (environment != null && environment.getActiveProfiles() != null
                && environment.getActiveProfiles().length > 0)
            this.activeProfiles = asList(environment.getActiveProfiles());
        else
            this.activeProfiles = asList(DEFAULT_PROFILE);
    }


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


    public List<MigrationInfo> fetchMigrations(Version applicationVersion) throws MigrationException
    {
        Reflections reflections = new Reflections(changeLogsBasePackage);
        List<MigrationInfo> migrations = new ArrayList<>();
        List<Class<?>> migrationClasses = new ArrayList<>(reflections.getTypesAnnotatedWith(Migration.class));
        for (Class<?> migrationClass : migrationClasses)
        {
            MigrationInfo migrationInfo = new MigrationInfo(migrationClass);
            // Add only versions lower or equal to current application version
            if (migrationInfo.getVersion().compareTo(applicationVersion) <= 0)
                migrations.add(migrationInfo);
        }
        migrations.sort(new MigrationComparator());
        return migrations;
    }


    public List<Method> fetchMigrationUnits(final Class<?> type) throws MigrationUnitException
    {
        final List<Method> changeSets = filterChangeSetAnnotation(asList(type.getDeclaredMethods()));
        Collections.sort(changeSets, new ChangeSetComparator());
        return changeSets;
    }


    public boolean isAlwaysRunnableMigration(Method changesetMethod)
    {
        if (!changesetMethod.isAnnotationPresent(MigrationUnit.class))
            return false;

        MigrationUnit annotation = changesetMethod.getAnnotation(MigrationUnit.class);
        return annotation.runAlways();
    }


    private List<Method> filterChangeSetAnnotation(List<Method> allMethods) throws MigrationUnitException
    {
        final Set<Integer> changeSetIds = new HashSet<>();
        final List<Method> changeSetMethods = new ArrayList<>();
        for (final Method method : allMethods)
        {
            if (method.isAnnotationPresent(MigrationUnit.class))
            {
                int id = method.getAnnotation(MigrationUnit.class).id();
                if (changeSetIds.contains(id))
                    throw new MigrationUnitException(String.format("Duplicated MigrationUnit id found: '%s'", id));

                changeSetIds.add(id);
                changeSetMethods.add(method);
            }
        }
        return changeSetMethods;
    }
}

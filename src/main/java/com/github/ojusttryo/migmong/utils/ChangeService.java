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

import com.github.ojusttryo.migmong.changeset.ChangeLog;
import com.github.ojusttryo.migmong.changeset.ChangeSet;
import com.github.ojusttryo.migmong.changeset.Migration;
import com.github.ojusttryo.migmong.exception.MigMongChangeSetException;
import com.github.ojusttryo.migmong.changeset.ChangeEntry;
import com.github.ojusttryo.migmong.exception.MigMongException;

/**
 * Utilities to deal with reflections and annotations
 *
 * @author lstolowski
 * @since 27/07/2014
 */
public class ChangeService
{
    private static final String DEFAULT_PROFILE = "default";

    private final String changeLogsBasePackage;
    private final List<String> activeProfiles;


    public ChangeService(String changeLogsBasePackage)
    {
        this(changeLogsBasePackage, null);
    }


    public ChangeService(String changeLogsBasePackage, Environment environment)
    {
        this.changeLogsBasePackage = changeLogsBasePackage;

        if (environment != null && environment.getActiveProfiles() != null
                && environment.getActiveProfiles().length > 0)
            this.activeProfiles = asList(environment.getActiveProfiles());
        else
            this.activeProfiles = asList(DEFAULT_PROFILE);
    }


    public ChangeEntry createChangeEntry(Method changesetMethod)
    {
        if (changesetMethod.isAnnotationPresent(ChangeSet.class))
        {
            ChangeSet annotation = changesetMethod.getAnnotation(ChangeSet.class);

            return new ChangeEntry(
                    annotation.id(),
                    new Date(),
                    changesetMethod.getDeclaringClass().getName(),
                    changesetMethod.getName());
        }
        else
        {
            return null;
        }
    }


    public List<Migration> fetchMigrations() throws MigMongException
    {
        Reflections reflections = new Reflections(changeLogsBasePackage);
        List<Class<?>> changeLogs = new ArrayList<>(reflections.getTypesAnnotatedWith(ChangeLog.class));
        List<Migration> migrations = new ArrayList<>();
        for (Class<?> changeLogClass : changeLogs)
            migrations.add(new Migration(changeLogClass));
        migrations.sort(new MigrationComparator());
        return migrations;
    }


    public List<Method> fetchChangeSets(final Class<?> type) throws MigMongChangeSetException
    {
        final List<Method> changeSets = filterChangeSetAnnotation(asList(type.getDeclaredMethods()));
        Collections.sort(changeSets, new ChangeSetComparator());
        return changeSets;
    }


    public boolean isRunAlwaysChangeSet(Method changesetMethod)
    {
        if (!changesetMethod.isAnnotationPresent(ChangeSet.class))
            return false;

        ChangeSet annotation = changesetMethod.getAnnotation(ChangeSet.class);
        return annotation.runAlways();
    }


    private List<Method> filterChangeSetAnnotation(List<Method> allMethods) throws MigMongChangeSetException
    {
        final Set<Integer> changeSetIds = new HashSet<>();
        final List<Method> changeSetMethods = new ArrayList<>();
        for (final Method method : allMethods)
        {
            if (method.isAnnotationPresent(ChangeSet.class))
            {
                int id = method.getAnnotation(ChangeSet.class).id();
                if (changeSetIds.contains(id))
                    throw new MigMongChangeSetException(String.format("Duplicated ChangeSet id found: '%s'", id));

                changeSetIds.add(id);
                changeSetMethods.add(method);
            }
        }
        return changeSetMethods;
    }
}

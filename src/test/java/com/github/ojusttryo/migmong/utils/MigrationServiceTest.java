package com.github.ojusttryo.migmong.utils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import com.github.ojusttryo.migmong.exception.MigrationException;
import com.github.ojusttryo.migmong.exception.MigrationUnitException;
import com.github.ojusttryo.migmong.migration.MigrationEntry;
import com.github.ojusttryo.migmong.migration.MigrationInfo;
import com.github.ojusttryo.migmong.migrationWithDuplicates.V_0_1_5__withDuplicates;
import com.github.ojusttryo.migmong.migrations.V_0_9__anotherMigrations;
import com.github.ojusttryo.migmong.migrations.V_1__migrations;

import junit.framework.Assert;

/**
 *
 * @author lstolowski
 * @since 27/07/2014
 */
public class MigrationServiceTest
{
    @Test
    public void shouldCreateEntry() throws MigrationUnitException
    {
        String scanPackage = V_1__migrations.class.getPackage().getName();
        MigrationService service = new MigrationService(scanPackage);
        List<Method> foundMethods = service.fetchMigrationUnits(V_1__migrations.class);

        for (Method foundMethod : foundMethods)
        {
            MigrationEntry entry = service.createMigrationEntry(foundMethod);

            Assert.assertEquals(V_1__migrations.class.getSimpleName(), entry.getMigrationClass());
            Assert.assertNotNull(entry.getTimestamp());
            Assert.assertNotNull(entry.getChangeId());
            Assert.assertNotNull(entry.getMigrationUnit());
        }
    }


    @Test(expected = MigrationUnitException.class)
    public void shouldFailOnDuplicatedMigrationUnits() throws MigrationUnitException
    {
        String scanPackage = V_0_1_5__withDuplicates.class.getPackage().getName();
        MigrationService service = new MigrationService(scanPackage);
        service.fetchMigrationUnits(V_0_1_5__withDuplicates.class);
    }


    @Test
    public void shouldFindMigrationClasses() throws MigrationException
    {
        String scanPackage = V_1__migrations.class.getPackage().getName();
        MigrationService service = new MigrationService(scanPackage);

        List<MigrationInfo> foundMigrations = service.fetchMigrations("V_");

        assertTrue(foundMigrations != null && foundMigrations.size() > 0);
    }


    @Test
    public void shouldFindMigrationUnits() throws MigrationUnitException
    {
        String scanPackage = V_1__migrations.class.getPackage().getName();
        MigrationService service = new MigrationService(scanPackage);

        List<Method> foundMethods = service.fetchMigrationUnits(V_1__migrations.class);

        assertTrue(foundMethods != null && foundMethods.size() == 4);
    }


    @Test
    public void shouldFindAnotherMigrationUnits() throws MigrationUnitException
    {
        String scanPackage = V_1__migrations.class.getPackage().getName();
        MigrationService service = new MigrationService(scanPackage);

        List<Method> foundMethods = service.fetchMigrationUnits(V_0_9__anotherMigrations.class);

        assertTrue(foundMethods != null && foundMethods.size() == 4);
    }


    @Test
    public void shouldFindIsRunAlwaysMethod() throws MigrationUnitException
    {
        String scanPackage = V_1__migrations.class.getPackage().getName();
        MigrationService service = new MigrationService(scanPackage);

        List<Method> foundMethods = service.fetchMigrationUnits(V_0_9__anotherMigrations.class);

        for (Method foundMethod : foundMethods)
        {
            boolean unitIsAlwaysRunnable = foundMethod.getName().contains("withAlways");
            boolean detectedAsAlwaysRunnable = service.isAlwaysRunnableMigration(foundMethod);
            assertEquals(unitIsAlwaysRunnable, detectedAsAlwaysRunnable);
        }
    }
}

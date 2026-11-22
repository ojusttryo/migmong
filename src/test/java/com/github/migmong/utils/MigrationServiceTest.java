package com.github.migmong.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.migmong.migrations.V_0_9__anotherMigrations;
import com.github.migmong.migrations.V_1__migrations;
import com.github.migmong.exception.MigrationException;
import com.github.migmong.exception.MigrationUnitException;
import com.github.migmong.migration.MigrationEntry;
import com.github.migmong.migration.MigrationInfo;
import com.github.migmong.migrationWithDuplicates.V_0_1_5__withDuplicates;

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

            assertEquals(V_1__migrations.class.getSimpleName(), entry.getMigrationClass());
            assertNotNull(entry.getTimestamp());
            assertNotNull(entry.getChangeId());
            assertNotNull(entry.getMigrationUnit());
        }
    }


    @Test
    public void shouldFailOnDuplicatedMigrationUnits() throws MigrationException
    {
        String scanPackage = V_0_1_5__withDuplicates.class.getPackage().getName();
        MigrationService service = new MigrationService(scanPackage);

        assertThrows(MigrationUnitException.class,
                () -> service.fetchMigrationUnits(V_0_1_5__withDuplicates.class));
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

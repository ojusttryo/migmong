package com.github.ojusttryo.migmong.utils;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import com.github.ojusttryo.migmong.exception.MigrationUnitException;
import com.github.ojusttryo.migmong.exception.MigrationException;
import com.github.ojusttryo.migmong.migration.MigrationInfo;
import com.github.ojusttryo.migmong.migration.Version;
import com.github.ojusttryo.migmong.test.changelogs.AnotherMongobeeTestResource;
import com.github.ojusttryo.migmong.test.changelogs.MongobeeTestResource;
import com.github.ojusttryo.migmong.migration.MigrationEntry;

import junit.framework.Assert;

/**
 * @author lstolowski
 * @since 27/07/2014
 */
public class MigrationServiceTest
{

    @Test
    public void shouldCreateEntry() throws MigrationUnitException
    {

        // given
        String scanPackage = MongobeeTestResource.class.getPackage().getName();
        MigrationService service = new MigrationService(scanPackage);
        List<Method> foundMethods = service.fetchMigrationUnits(MongobeeTestResource.class);

        for (Method foundMethod : foundMethods)
        {

            // when
            MigrationEntry entry = service.createMigrationEntry(foundMethod);

            // then
            Assert.assertEquals(MongobeeTestResource.class.getName(), entry.getChangeLogClass());
            Assert.assertNotNull(entry.getTimestamp());
            Assert.assertNotNull(entry.getChangeId());
            Assert.assertNotNull(entry.getChangeSetMethodName());
        }
    }


    @Test(expected = MigrationUnitException.class)
    public void shouldFailOnDuplicatedChangeSets() throws MigrationUnitException
    {
        String scanPackage = ChangeLogWithDuplicate.class.getPackage().getName();
        MigrationService service = new MigrationService(scanPackage);
        service.fetchMigrationUnits(ChangeLogWithDuplicate.class);
    }


    @Test
    public void shouldFindAnotherChangeSetMethods() throws MigrationUnitException
    {
        // given
        String scanPackage = MongobeeTestResource.class.getPackage().getName();
        MigrationService service = new MigrationService(scanPackage);

        // when
        List<Method> foundMethods = service.fetchMigrationUnits(AnotherMongobeeTestResource.class);

        // then
        assertTrue(foundMethods != null && foundMethods.size() == 6);
    }


    @Test
    public void shouldFindChangeLogClasses() throws MigrationException
    {
        // given
        String scanPackage = MongobeeTestResource.class.getPackage().getName();
        MigrationService service = new MigrationService(scanPackage);
        // when
        List<MigrationInfo> foundMigrations = service.fetchMigrations(new Version(1, 1, 1));
        // then
        assertTrue(foundMigrations != null && foundMigrations.size() > 0);
    }


    @Test
    public void shouldFindChangeSetMethods() throws MigrationUnitException
    {
        // given
        String scanPackage = MongobeeTestResource.class.getPackage().getName();
        MigrationService service = new MigrationService(scanPackage);

        // when
        List<Method> foundMethods = service.fetchMigrationUnits(MongobeeTestResource.class);

        // then
        assertTrue(foundMethods != null && foundMethods.size() == 5);
    }


    @Test
    public void shouldFindIsRunAlwaysMethod() throws MigrationUnitException
    {
        // given
        String scanPackage = MongobeeTestResource.class.getPackage().getName();
        MigrationService service = new MigrationService(scanPackage);

        // when
        List<Method> foundMethods = service.fetchMigrationUnits(AnotherMongobeeTestResource.class);
        // then
        for (Method foundMethod : foundMethods)
        {
            if (foundMethod.getName().equals("testChangeSetWithAlways"))
            {
                assertTrue(service.isAlwaysRunnableMigration(foundMethod));
            }
            else
            {
                assertFalse(service.isAlwaysRunnableMigration(foundMethod));
            }
        }
    }

}

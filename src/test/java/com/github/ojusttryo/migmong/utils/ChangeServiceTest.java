package com.github.ojusttryo.migmong.utils;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import com.github.ojusttryo.migmong.test.changelogs.AnotherMongobeeTestResource;
import com.github.ojusttryo.migmong.test.changelogs.MongobeeTestResource;
import com.github.ojusttryo.migmong.changeset.ChangeEntry;
import com.github.ojusttryo.migmong.exception.MigMongChangeSetException;

import junit.framework.Assert;

/**
 * @author lstolowski
 * @since 27/07/2014
 */
public class ChangeServiceTest
{

    @Test
    public void shouldCreateEntry() throws MigMongChangeSetException
    {

        // given
        String scanPackage = MongobeeTestResource.class.getPackage().getName();
        ChangeService service = new ChangeService(scanPackage);
        List<Method> foundMethods = service.fetchChangeSets(MongobeeTestResource.class);

        for (Method foundMethod : foundMethods)
        {

            // when
            ChangeEntry entry = service.createChangeEntry(foundMethod);

            // then
            Assert.assertEquals("testuser", entry.getAuthor());
            Assert.assertEquals(MongobeeTestResource.class.getName(), entry.getChangeLogClass());
            Assert.assertNotNull(entry.getTimestamp());
            Assert.assertNotNull(entry.getChangeId());
            Assert.assertNotNull(entry.getChangeSetMethodName());
        }
    }


    @Test(expected = MigMongChangeSetException.class)
    public void shouldFailOnDuplicatedChangeSets() throws MigMongChangeSetException
    {
        String scanPackage = ChangeLogWithDuplicate.class.getPackage().getName();
        ChangeService service = new ChangeService(scanPackage);
        service.fetchChangeSets(ChangeLogWithDuplicate.class);
    }


    @Test
    public void shouldFindAnotherChangeSetMethods() throws MigMongChangeSetException
    {
        // given
        String scanPackage = MongobeeTestResource.class.getPackage().getName();
        ChangeService service = new ChangeService(scanPackage);

        // when
        List<Method> foundMethods = service.fetchChangeSets(AnotherMongobeeTestResource.class);

        // then
        assertTrue(foundMethods != null && foundMethods.size() == 6);
    }


    @Test
    public void shouldFindChangeLogClasses()
    {
        // given
        String scanPackage = MongobeeTestResource.class.getPackage().getName();
        ChangeService service = new ChangeService(scanPackage);
        // when
        List<Class<?>> foundClasses = service.fetchChangeLogs();
        // then
        assertTrue(foundClasses != null && foundClasses.size() > 0);
    }


    @Test
    public void shouldFindChangeSetMethods() throws MigMongChangeSetException
    {
        // given
        String scanPackage = MongobeeTestResource.class.getPackage().getName();
        ChangeService service = new ChangeService(scanPackage);

        // when
        List<Method> foundMethods = service.fetchChangeSets(MongobeeTestResource.class);

        // then
        assertTrue(foundMethods != null && foundMethods.size() == 5);
    }


    @Test
    public void shouldFindIsRunAlwaysMethod() throws MigMongChangeSetException
    {
        // given
        String scanPackage = MongobeeTestResource.class.getPackage().getName();
        ChangeService service = new ChangeService(scanPackage);

        // when
        List<Method> foundMethods = service.fetchChangeSets(AnotherMongobeeTestResource.class);
        // then
        for (Method foundMethod : foundMethods)
        {
            if (foundMethod.getName().equals("testChangeSetWithAlways"))
            {
                assertTrue(service.isRunAlwaysChangeSet(foundMethod));
            }
            else
            {
                assertFalse(service.isRunAlwaysChangeSet(foundMethod));
            }
        }
    }

}

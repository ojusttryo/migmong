package com.github.ojusttryo.migmong.changeset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set of changes to be added to the DB. Many instances of {@link ChangeSet} are included in one ChangeLog.
 * @author lstolowski
 * @since 27/07/2014
 * @see ChangeLog
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeSet
{
    /**
     * Unique ID of the {@link ChangeSet}. Sets the order of migrations within the ChangeSet instance.
     * Obligatory
     * @return unique id
     */
    int id();


    /**
     * Executes the change set on every MigMong's execution, even if it has been run before.
     * Optional (default is false)
     * @return should run always?
     */
    boolean runAlways() default false;
}

package com.github.ojusttryo.migmong.changeset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set of changes to be added to the DB. Many changesets are included in one changelog.
 * @author lstolowski
 * @since 27/07/2014
 * @see ChangeLog
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeSet
{
    /**
     * Author of the {@link ChangeSet}.
     * Obligatory
     * @return author
     */
    String author();  // must be set


    /**
     * Unique ID of the {@link ChangeSet}.
     * Obligatory
     * @return unique id
     */
    String id();      // must be set


    /**
     * Sequence that provide correct order for {@link ChangeSet}. Sorted alphabetically, ascending.
     * Obligatory.
     * @return ordering
     */
    String order();   // must be set


    /**
     * Executes the change set on every MigMong's execution, even if it has been run before.
     * Optional (default is false)
     * @return should run always?
     */
    boolean runAlways() default false;
}

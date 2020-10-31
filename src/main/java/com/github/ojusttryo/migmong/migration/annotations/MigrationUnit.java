package com.github.ojusttryo.migmong.migration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method with changes to be added to the database. Many instances of {@link MigrationUnit} are included in one
 * {@link Migration}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MigrationUnit
{
    /**
     * Unique ID of the {@link MigrationUnit}. Sets the order of migrations within the MigrationUnit instance.
     * Obligatory
     * @return unique id
     */
    int id();


    /**
     * Executes current changes on every migration execution, even if it has been run before.
     * Optional (default is false)
     * @return should run always or not
     */
    boolean runAlways() default false;
}

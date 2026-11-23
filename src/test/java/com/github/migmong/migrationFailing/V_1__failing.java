package com.github.migmong.migrationFailing;

import com.github.migmong.migration.MigrationContext;
import com.github.migmong.migration.annotations.Migration;
import com.github.migmong.migration.annotations.MigrationUnit;

/**
 * Migration which always fails. Used to check that the lock is released after a failure.
 */
@Migration
public class V_1__failing
{
    @MigrationUnit(id = 1)
    public void failingMigration(MigrationContext context)
    {
        throw new RuntimeException("Intentional migration failure");
    }
}

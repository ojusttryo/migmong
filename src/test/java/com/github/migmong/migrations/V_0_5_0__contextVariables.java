package com.github.migmong.migrations;

import com.github.migmong.migration.MigrationContext;
import com.github.migmong.migration.annotations.Migration;
import com.github.migmong.migration.annotations.MigrationUnit;

/**
 * Tests passing context into migration units
 */
@Migration
public class V_0_5_0__contextVariables
{
    @MigrationUnit(id = 4)
    public void testContextVariables(MigrationContext context)
    {
        System.out.println("invoked with migration context");
    }
}

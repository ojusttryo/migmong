package com.github.ojusttryo.migmong.migration;

import java.util.Arrays;
import java.util.List;

import com.github.ojusttryo.migmong.exception.MigrationException;

public class MigrationInfo
{
    private Class<?> migrationClass;
    private Version version;


    public Class<?> getMigrationClass()
    {
        return migrationClass;
    }


    public Version getVersion()
    {
        return version;
    }


    public MigrationInfo(Class<?> migrationClass) throws MigrationException
    {
        this.migrationClass = migrationClass;
        this.version = parseVersion(migrationClass.getSimpleName());
    }


    private Version parseVersion(String className) throws MigrationException
    {
        String version = className.replaceFirst("V_", "");
        version = version.replaceFirst("__\\w+\\Z", "");

        return Version.from(version, "_");
    }
}


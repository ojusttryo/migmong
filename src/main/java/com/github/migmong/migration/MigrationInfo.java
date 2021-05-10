package com.github.migmong.migration;

import java.util.regex.Pattern;

import com.github.migmong.exception.MigrationException;

import lombok.Data;

@Data
public class MigrationInfo
{
    private Class<?> migrationClass;
    private Version version;


    public MigrationInfo(Class<?> migrationClass, String prefix) throws MigrationException
    {
        this.migrationClass = migrationClass;
        this.version = parseVersion(migrationClass.getSimpleName(), prefix);
    }


    private Version parseVersion(String className, String prefix) throws MigrationException
    {
        String version = className.replaceFirst(Pattern.quote(prefix), "");
        version = version.replaceFirst("__\\w+\\Z", "");

        return Version.from(version, "_");
    }
}


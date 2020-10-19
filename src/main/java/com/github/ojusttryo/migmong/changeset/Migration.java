package com.github.ojusttryo.migmong.changeset;

import java.util.Arrays;
import java.util.List;

import com.github.ojusttryo.migmong.exception.MigMongException;

public class Migration
{
    private Class<?> changeLogClass;
    private Version version;


    public Class<?> getChangeLogClass()
    {
        return changeLogClass;
    }


    public Version getVersion()
    {
        return version;
    }


    public Migration(Class<?> changeLogClass) throws MigMongException
    {
        this.changeLogClass = changeLogClass;
        this.version = parseVersion(changeLogClass.getCanonicalName());
    }


    private Version parseVersion(String className) throws MigMongException
    {
        className = className.replaceFirst("V", "");
        className.replaceFirst("__[a-zA-Z0-9]+\\Z", "");
        List<String> numbers = Arrays.asList(className.split("_"));
        if (numbers.size() == 0 || numbers.size() > 3 || numbers.stream().anyMatch(x -> !x.matches("[0-9]+")))
            throw new MigMongException("Wrong version number in migration " + className);

        int major = Integer.parseInt(numbers.get(0));
        int minor = 0;
        int build = 0;
        if (numbers.size() > 1)
            minor = Integer.parseInt(numbers.get(1));
        if (numbers.size() > 2)
            build = Integer.parseInt(numbers.get(2));

        return new Version(major, minor, build);
    }
}


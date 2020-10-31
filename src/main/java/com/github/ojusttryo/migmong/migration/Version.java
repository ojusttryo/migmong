package com.github.ojusttryo.migmong.migration;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents the version of application, migration, etc.
 */
public class Version
{
    /**
     * Get version from string
     * @param version version like '1' of '0.5.3'. Min count of numbers is 1, max is 3
     * @param delimiter delimiter for numbers in version, e.g. '.' or '_'
     * @return prepared {@link Version} object
     */
    public static Version from(String version, String delimiter) throws IllegalArgumentException
    {
        List<String> numbers = Arrays.asList(version.split(Pattern.quote(delimiter)));
        if (numbers.size() == 0 || numbers.size() > 3 || numbers.stream().anyMatch(x -> !x.matches("[0-9]+")))
            throw new IllegalArgumentException(String.format("Wrong version number (%s)", version));

        int major = Integer.parseInt(numbers.get(0));
        int minor = 0;
        int build = 0;
        if (numbers.size() > 1)
            minor = Integer.parseInt(numbers.get(1));
        if (numbers.size() > 2)
            build = Integer.parseInt(numbers.get(2));

        return new Version(major, minor, build);
    }


    private int major;
    private int minor;
    private int build;


    public Version()
    {
        this(0, 0, 0);
    }


    public Version(int major, int minor, int build)
    {
        if (major < 0 || minor < 0 || build < 0)
            throw new IllegalArgumentException("Version number can not be negative");

        this.major = major;
        this.minor = minor;
        this.build = build;
    }


    public int getMajor()
    {
        return major;
    }

    public int getMinor()
    {
        return minor;
    }

    public int getBuild()
    {
        return build;
    }

    public int compareTo(Version version)
    {
        Version left = this;
        Version right = version;

        boolean majorEquals = left.getMajor() == right.getMajor();
        boolean minorEquals = left.getMinor() == right.getMinor();
        boolean buildEquals = left.getBuild() == right.getBuild();
        if (majorEquals && minorEquals && buildEquals)
            return 0;

        if (left.getMajor() < right.getMajor()
                || majorEquals && left.getMinor() < right.getMinor()
                || majorEquals && minorEquals && left.getBuild() < right.getBuild())
            return -1;
        else
            return 1;
    }

    @Override
    public String toString()
    {
        return String.format("%d_%d_%d", major, minor, build);
    }
}

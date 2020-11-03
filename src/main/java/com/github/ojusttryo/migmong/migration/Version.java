package com.github.ojusttryo.migmong.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents the version of application, migration, etc.
 */
public class Version
{
    /**
     * Get version from string
     * @param version version like '1' or '0.5.3'. There are no limit for numbers count.
     * @param delimiter delimiter for numbers in version, e.g. '.' or '_'
     * @return prepared {@link Version} object
     */
    public static Version from(String version, String delimiter) throws IllegalArgumentException
    {
        List<String> numbers = Arrays.asList(version.split(Pattern.quote(delimiter)));
        if (numbers.size() == 0 || numbers.stream().anyMatch(x -> !x.matches("[0-9]+")))
            throw new IllegalArgumentException(String.format("Wrong version number (%s)", version));

        return new Version(numbers.stream().map(Integer::parseInt).toArray(Integer[]::new));
    }


    private List<Integer> versionNumbers = new ArrayList<>();


    public Version()
    {
        this(0);
    }


    public Version(Integer... versionNumbers)
    {
        for (int i = 0; i < versionNumbers.length; i++)
        {
            if (versionNumbers[i] == null || versionNumbers[i] < 0)
                throw new IllegalArgumentException("Wrong version number");

            this.versionNumbers.add(versionNumbers[i]);
        }
    }


    public int compareTo(Version version)
    {
        Version left = this;
        Version right = version;

        int maxNumbers = Math.max(left.versionNumbers.size(), right.versionNumbers.size());
        List<Integer> compareResult = new ArrayList<>();
        for (int i = 0; i < maxNumbers; i++)
        {
            // Versions can have different count of numbers. In these cases we add zero to make the same count.
            Integer leftNumber = left.versionNumbers.size() > i ? left.versionNumbers.get(i) : 0;
            Integer rightNumber = right.versionNumbers.size() > i ? right.versionNumbers.get(i) : 0;

            compareResult.add(leftNumber.compareTo(rightNumber));
        }

        for (int i = 0; i < maxNumbers; i++)
        {
            if (compareResult.get(i) != 0)
                return compareResult.get(i);
        }

        return 0;
    }

    @Override
    public String toString()
    {
        return versionNumbers.stream().map(Object::toString).collect(Collectors.joining("."));
    }
}

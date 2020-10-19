package com.github.ojusttryo.migmong.changeset;


/**
 * Represents the version of application, migration, etc.
 */
public class Version
{
    private int major;
    private int minor;
    private int build;
    

    public Version(int major, int minor, int build)
    {
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

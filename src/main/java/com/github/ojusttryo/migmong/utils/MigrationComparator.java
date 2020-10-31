package com.github.ojusttryo.migmong.utils;

import java.util.Comparator;

import com.github.ojusttryo.migmong.migration.MigrationInfo;

public class MigrationComparator implements Comparator<MigrationInfo>
{
    @Override
    public int compare(MigrationInfo left, MigrationInfo right)
    {
        if (left == null && right == null)
            return 0;
        else if (left == null)
            return -1;
        else if (right == null)
            return 1;

        return left.getVersion().compareTo(right.getVersion());
    }
}
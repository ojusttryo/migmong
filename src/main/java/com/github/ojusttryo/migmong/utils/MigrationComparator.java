package com.github.ojusttryo.migmong.utils;

import java.util.Comparator;

import com.github.ojusttryo.migmong.changeset.Migration;

public class MigrationComparator implements Comparator<Migration>
{
    @Override
    public int compare(Migration left, Migration right)
    {
        if (left == null && right == null)
            return 0;
        else if (left == null)
            return -1;
        else if (right == null)
            return 1;

        return  left.getVersion().compareTo(right.getVersion());
    }
}
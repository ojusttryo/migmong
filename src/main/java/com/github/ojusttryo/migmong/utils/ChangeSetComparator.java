package com.github.ojusttryo.migmong.utils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;

import com.github.ojusttryo.migmong.changeset.ChangeSet;

/**
 * Sort {@link ChangeSet} by 'order' value
 *
 * @author lstolowski
 * @since 2014-09-17
 */
public class ChangeSetComparator implements Comparator<Method>, Serializable
{
    @Override
    public int compare(Method left, Method right)
    {
        ChangeSet c1 = left.getAnnotation(ChangeSet.class);
        ChangeSet c2 = right.getAnnotation(ChangeSet.class);
        return c1.order().compareTo(c2.order());
    }
}

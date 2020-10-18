package com.github.ojusttryo.migmong.utils;

import static org.springframework.util.StringUtils.hasText;

import java.io.Serializable;
import java.util.Comparator;

import com.github.ojusttryo.migmong.changeset.ChangeLog;

/**
 * Sort {@link ChangeLog} by 'order' value or class name (if no 'order' is set)
 *
 * @author lstolowski
 * @since 2014-09-17
 */
public class ChangeLogComparator implements Comparator<Class<?>>, Serializable
{
    @Override
    public int compare(Class<?> left, Class<?> right)
    {
        ChangeLog changeLog1 = left.getAnnotation(ChangeLog.class);
        ChangeLog changeLog2 = right.getAnnotation(ChangeLog.class);

        String val1 = !(hasText(changeLog1.order())) ? left.getCanonicalName() : changeLog1.order();
        String val2 = !(hasText(changeLog2.order())) ? right.getCanonicalName() : changeLog2.order();

        if (val1 == null && val2 == null)
            return 0;
        else if (val1 == null)
            return -1;
        else if (val2 == null)
            return 1;

        return val1.compareTo(val2);
    }
}

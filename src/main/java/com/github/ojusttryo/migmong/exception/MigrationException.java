package com.github.ojusttryo.migmong.exception;

/**
 * General migration exception
 */
public class MigrationException extends Exception
{
    public MigrationException(String message)
    {
        super(message);
    }


    public MigrationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

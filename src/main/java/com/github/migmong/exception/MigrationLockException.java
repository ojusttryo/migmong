package com.github.migmong.exception;

/**
 * Error while can not obtain process lock
 */
public class MigrationLockException extends MigrationException
{
    public MigrationLockException(String message)
    {
        super(message);
    }
}

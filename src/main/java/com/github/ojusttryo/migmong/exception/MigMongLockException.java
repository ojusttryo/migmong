package com.github.ojusttryo.migmong.exception;

/**
 * Error while can not obtain process lock
 */
public class MigMongLockException extends MigMongException
{
    public MigMongLockException(String message)
    {
        super(message);
    }
}

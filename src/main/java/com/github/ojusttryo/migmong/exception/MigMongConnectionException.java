package com.github.ojusttryo.migmong.exception;

/**
 * Error while connection to MongoDB
 *
 * @author lstolowski
 * @since 27/07/2014
 */
public class MigMongConnectionException extends MigMongException
{
    public MigMongConnectionException(String message, Exception baseException)
    {
        super(message, baseException);
    }
}

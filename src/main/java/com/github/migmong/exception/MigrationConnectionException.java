package com.github.migmong.exception;

/**
 * Error while connection to MongoDB
 *
 * @author lstolowski
 * @since 27/07/2014
 */
public class MigrationConnectionException extends MigrationException
{
    public MigrationConnectionException(String message, Exception baseException)
    {
        super(message, baseException);
    }
}

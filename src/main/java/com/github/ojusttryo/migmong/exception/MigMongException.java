package com.github.ojusttryo.migmong.exception;

/**
 * General MingMong exception
 * @author abelski
 */
public class MigMongException extends Exception
{
    public MigMongException(String message)
    {
        super(message);
    }


    public MigMongException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

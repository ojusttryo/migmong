package com.github.ojusttryo.migmong;

import static com.mongodb.ServerAddress.defaultHost;
import static com.mongodb.ServerAddress.defaultPort;
import static org.springframework.util.StringUtils.hasText;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.github.ojusttryo.migmong.changeset.Migration;
import com.github.ojusttryo.migmong.dao.ChangeEntryDao;
import com.github.ojusttryo.migmong.exception.MigMongChangeSetException;
import com.github.ojusttryo.migmong.changeset.ChangeEntry;
import com.github.ojusttryo.migmong.exception.MigMongConfigurationException;
import com.github.ojusttryo.migmong.exception.MigMongConnectionException;
import com.github.ojusttryo.migmong.exception.MigMongException;
import com.github.ojusttryo.migmong.utils.ChangeService;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ReadConcern;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;

/**
 * MigMong runner
 *
 * @author lstolowski
 * @since 26/07/2014
 */
public class MigMong implements InitializingBean
{
    private static final Logger logger = LoggerFactory.getLogger(MigMong.class);

    private static final String DEFAULT_CHANGELOG_COLLECTION_NAME = "dbchangelog";
    private static final String DEFAULT_LOCK_COLLECTION_NAME = "mongobeelock";
    private static final boolean DEFAULT_WAIT_FOR_LOCK = false;
    private static final long DEFAULT_CHANGE_LOG_LOCK_WAIT_TIME = 5L;
    private static final long DEFAULT_CHANGE_LOG_LOCK_POLL_RATE = 10L;
    private static final boolean DEFAULT_THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK = false;

    private ChangeEntryDao dao;

    private boolean enabled = true;
    private String changeLogsScanPackage;
    private MongoClientURI mongoClientURI;
    private MongoClient mongoClient;
    private String dbName;
    private Environment springEnvironment;
    private ApplicationContext applicationContext;
    private MongoTemplate mongoTemplate;


    /**
     * <p>Simple constructor with default configuration of host (localhost) and port (27017). Although
     * <b>the database name need to be provided</b> using {@link MigMong#setDbName(String)} setter.</p>
     * <p>It is recommended to use constructors with MongoURI</p>
     */
    public MigMong()
    {
        this(new MongoClientURI("mongodb://" + defaultHost() + ":" + defaultPort() + "/"));
    }


    /**
     * <p>Constructor takes db.mongodb.MongoClientURI object as a parameter.
     * </p><p>For more details about MongoClientURI please see com.mongodb.MongoClientURI docs
     * </p>
     *
     * @param mongoClientURI uri to your db
     * @see MongoClientURI
     */
    public MigMong(MongoClientURI mongoClientURI)
    {
        this.mongoClientURI = mongoClientURI;
        this.setDbName(mongoClientURI.getDatabase());
        this.dao = createDao();
    }


    /**
     * <p>Constructor takes db.mongodb.MongoClient object as a parameter.
     * </p><p>For more details about <tt>MongoClient</tt> please see com.mongodb.MongoClient docs
     * </p>
     *
     * @param mongoClient database connection client
     * @see MongoClient
     */
    public MigMong(MongoClient mongoClient)
    {
        this.mongoClient = mongoClient;
        this.dao = createDao();
    }


    /**
     * <p>MigMong runner. Correct MongoDB URI should be provided.</p>
     * <p>The format of the URI is:
     * <pre>
     *   mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database[
     *   .collection]][?options]]
     * </pre>
     * <ul>
     * <li>{@code mongodb://} Required prefix</li>
     * <li>{@code username:password@} are optional.  If given, the driver will attempt to login to a database after
     * connecting to a database server. For some authentication mechanisms, only the username is specified and the
     * password is not,
     * in which case the ":" after the username is left off as well.</li>
     * <li>{@code host1} Required.  It identifies a server address to connect to. More than one host can be provided
     * .</li>
     * <li>{@code :portX} is optional and defaults to :27017 if not provided.</li>
     * <li>{@code /database} the name of the database to login to and thus is only relevant if the
     * {@code username:password@} syntax is used. If not specified the "admin" database will be used by default.
     * <b>MigMong will operate on the database provided here or on the database overriden by setter setDbName(String)
     * .</b>
     * </li>
     * <li>{@code ?options} are connection options. For list of options please see com.mongodb.MongoClientURI docs</li>
     * </ul>
     * <p>For details, please see com.mongodb.MongoClientURI
     *
     * @param mongoURI with correct format
     * @see com.mongodb.MongoClientURI
     */
    public MigMong(String mongoURI)
    {
        this(new MongoClientURI(mongoURI));
    }


    public MigMong(String host, Integer port, String dbName, String user, String password)
    {
        this.dbName = dbName;
        MongoCredential credential = MongoCredential.createCredential(user, dbName, password.toCharArray());
        ServerAddress address = new ServerAddress(host, port);
        MongoClientOptions options = MongoClientOptions
                .builder()
                .writeConcern(WriteConcern.ACKNOWLEDGED)
                .readConcern(ReadConcern.LOCAL)
                .build();
        mongoClient = new MongoClient(address, credential, options);
        this.dao = createDao();
    }


    /**
     * For Spring users: executing migmong after bean is created in the Spring context
     *
     * @throws Exception exception
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        execute();
    }


    /**
     * Closes the Mongo instance used by MigMong.
     * This will close either the connection MigMong was initiated with or that which was internally created.
     */
    public void close()
    {
        dao.close();
    }


    /**
     * Executing migration
     *
     * @throws MigMongException exception
     */
    public void execute() throws MigMongException
    {
        if (!isEnabled())
        {
            logger.info("MigMong is disabled. Exiting.");
            return;
        }

        validateConfig();

        if (this.mongoClient != null)
        {
            dao.connectMongoDb(this.mongoClient, dbName);
        }
        else
        {
            dao.connectMongoDb(this.mongoClientURI, dbName);
        }

        if (!dao.acquireProcessLock())
        {
            logger.info("MigMong did not acquire process lock. Exiting.");
            return;
        }

        logger.info("MigMong acquired process lock, starting the data migration sequence..");

        try
        {
            executeMigration();
        }
        finally
        {
            logger.info("MigMong is releasing process lock.");
            dao.releaseProcessLock();
        }

        logger.info("MigMong has finished his job.");
    }


    /**
     * @return true if MigMong runner is enabled and able to run, otherwise false
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    /**
     * @return true if an execution is in progress, in any process.
     * @throws MigMongConnectionException exception
     */
    public boolean isExecutionInProgress() throws MigMongConnectionException
    {
        return dao.isProccessLockHeld();
    }


    public ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }


    /**
     * Poll rate for acquiring lock if waitForLock is true
     *
     * @param changeLogLockPollRate Poll rate in seconds for acquiring lock
     * @return MigMong object for fluent interface
     */
    public MigMong setChangeLogLockPollRate(long changeLogLockPollRate)
    {
        this.dao.setChangeLogLockPollRate(changeLogLockPollRate);
        return this;
    }


    /**
     * Waiting time for acquiring lock if waitForLock is true
     *
     * @param changeLogLockWaitTime Waiting time in minutes for acquiring lock
     * @return MigMong object for fluent interface
     */
    public MigMong setChangeLogLockWaitTime(long changeLogLockWaitTime)
    {
        this.dao.setChangeLogLockWaitTime(changeLogLockWaitTime);
        return this;
    }


    /**
     * Package name where @ChangeLog-annotated classes are kept.
     *
     * @param changeLogsScanPackage package where your changelogs are
     * @return MigMong object for fluent interface
     */
    public MigMong setChangeLogsScanPackage(String changeLogsScanPackage)
    {
        this.changeLogsScanPackage = changeLogsScanPackage;
        return this;
    }


    /**
     * Overwrites a default mongobee changelog collection hardcoded in DEFAULT_CHANGELOG_COLLECTION_NAME.
     *
     * CAUTION! Use this method carefully - when changing the name on a existing system,
     * your changelogs will be executed again on your MongoDB instance
     *
     * @param changelogCollectionName a new changelog collection name
     * @return MigMong object for fluent interface
     */
    public MigMong setChangelogCollectionName(String changelogCollectionName)
    {
        this.dao.setChangelogCollectionName(changelogCollectionName);
        return this;
    }


    /**
     * Used DB name should be set here or via MongoDB URI (in a constructor)
     *
     * @param dbName database name
     * @return MigMong object for fluent interface
     */
    public MigMong setDbName(String dbName)
    {
        this.dbName = dbName;
        return this;
    }


    /**
     * Feature which enables/disables MigMong runner execution
     *
     * @param enabled MOngobee will run only if this option is set to true
     * @return MigMong object for fluent interface
     */
    public MigMong setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }


    /**
     * Overwrites a default mongobee lock collection hardcoded in DEFAULT_LOCK_COLLECTION_NAME
     *
     * @param lockCollectionName a new lock collection name
     * @return MigMong object for fluent interface
     */
    public MigMong setLockCollectionName(String lockCollectionName)
    {
        this.dao.setLockCollectionName(lockCollectionName);
        return this;
    }


    /**
     * Sets uri to MongoDB
     *
     * @param mongoClientURI object with defined mongo uri
     * @return MigMong object for fluent interface
     */
    public MigMong setMongoClientURI(MongoClientURI mongoClientURI)
    {
        this.mongoClientURI = mongoClientURI;
        return this;
    }


    /**
     * Sets pre-configured {@link MongoTemplate} instance to use by the MigMong
     *
     * @param mongoTemplate instance of the {@link MongoTemplate}
     * @return MigMong object for fluent interface
     */
    public MigMong setMongoTemplate(MongoTemplate mongoTemplate)
    {
        this.mongoTemplate = mongoTemplate;
        return this;
    }


    /**
     * Set Environment object for Spring Profiles (@Profile) integration
     *
     * @param environment org.springframework.core.env.Environment object to inject
     * @return MigMong object for fluent interface
     */
    public MigMong setSpringEnvironment(Environment environment)
    {
        this.springEnvironment = environment;
        return this;
    }


    /**
     * Feature which enables/disables throwing MigMongLockException if MigMong can not obtain lock
     *
     * @param throwExceptionIfCannotObtainLock MigMong will throw MigMongLockException if lock can not be obtained
     * @return MigMong object for fluent interface
     */
    public MigMong setThrowExceptionIfCannotObtainLock(boolean throwExceptionIfCannotObtainLock)
    {
        this.dao.setThrowExceptionIfCannotObtainLock(throwExceptionIfCannotObtainLock);
        return this;
    }


    /**
     * Feature which enables/disables waiting for lock if it's already obtained
     *
     * @param waitForLock MigMong will be waiting for lock if it's already obtained if this option is set to true
     * @return MigMong object for fluent interface
     */
    public MigMong setWaitForLock(boolean waitForLock)
    {
        this.dao.setWaitForLock(waitForLock);
        return this;
    }


    public MigMong setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
        return this;
    }


    private void executeMigration() throws MigMongException
    {
        ChangeService service = new ChangeService(changeLogsScanPackage, springEnvironment);

        for (Migration migration : service.fetchMigrations())
        {
            Class<?> changeLogClass = migration.getChangeLogClass();

            try
            {
                Object changelogInstance = changeLogClass.getConstructor().newInstance();
                List<Method> changeSetMethods = service.fetchChangeSets(changelogInstance.getClass());

                for (Method changeSetMethod : changeSetMethods)
                {
                    ChangeEntry changeEntry = service.createChangeEntry(changeSetMethod);

                    try
                    {
                        if (dao.isNewChange(changeEntry))
                        {
                            executeChangeSetMethod(changeSetMethod, changelogInstance, dao.getMongoDatabase());
                            dao.save(changeEntry);
                            logger.info(changeEntry + " applied");
                        }
                        else if (service.isRunAlwaysChangeSet(changeSetMethod))
                        {
                            executeChangeSetMethod(changeSetMethod, changelogInstance, dao.getMongoDatabase());
                            logger.info(changeEntry + " applied");
                        }
                        else
                        {
                            logger.info(changeEntry + " passed over");
                        }
                    }
                    catch (MigMongChangeSetException e)
                    {
                        logger.error(e.getMessage());
                    }
                }
            }
            catch (NoSuchMethodException e)
            {
                throw new MigMongException(e.getMessage(), e);
            }
            catch (IllegalAccessException e)
            {
                throw new MigMongException(e.getMessage(), e);
            }
            catch (InvocationTargetException e)
            {
                Throwable targetException = e.getTargetException();
                throw new MigMongException(targetException.getMessage(), e);
            }
            catch (InstantiationException e)
            {
                throw new MigMongException(e.getMessage(), e);
            }
        }
    }


    private Object executeChangeSetMethod(Method changeSetMethod, Object changeLogInstance, MongoDatabase mongoDatabase)
            throws IllegalAccessException, InvocationTargetException, MigMongChangeSetException,
            MigMongConfigurationException
    {
        if (methodHasParameters(changeSetMethod, MongoTemplate.class))
        {
            logger.debug("method with MongoTemplate argument");
            if (mongoTemplate == null)
                throw new MigMongConfigurationException("MongoTemplate is not set for " + changeSetMethod.getName());
            return changeSetMethod.invoke(changeLogInstance, mongoTemplate);
        }
        else if (methodHasParameters(changeSetMethod, MongoTemplate.class, Environment.class))
        {
            logger.debug("method with MongoTemplate and environment arguments");
            if (mongoTemplate == null)
                throw new MigMongConfigurationException("MongoTemplate is not set for " + changeSetMethod.getName());
            return changeSetMethod.invoke(changeLogInstance, mongoTemplate, springEnvironment);
        }
        else if (methodHasParameters(changeSetMethod, MongoTemplate.class, ApplicationContext.class))
        {
            logger.debug("method with MongoTemplate argument and application context");
            if (mongoTemplate == null)
                throw new MigMongConfigurationException("MongoTemplate is not set for " + changeSetMethod.getName());
            return changeSetMethod.invoke(changeLogInstance, mongoTemplate, applicationContext);
        }
        else if (methodHasParameters(changeSetMethod, MongoDatabase.class))
        {
            logger.debug("method with DB argument");
            return changeSetMethod.invoke(changeLogInstance, mongoDatabase);
        }
        else if (methodHasParameters(changeSetMethod, MongoDatabase.class, ApplicationContext.class))
        {
            logger.debug("method with DB argument and application context");
            return changeSetMethod.invoke(changeLogInstance, mongoDatabase, applicationContext);
        }
        else if (changeSetMethod.getParameterTypes().length == 0)
        {
            logger.debug("method with no params");
            return changeSetMethod.invoke(changeLogInstance);
        }
        else
        {
            throw new MigMongChangeSetException("ChangeSet method " + changeSetMethod.getName() +
                    " has wrong arguments list. Please see docs for more info!");
        }
    }


    private boolean methodHasParameters(Method changeSetMethod, Class... parameters)
    {
        if (changeSetMethod.getParameterTypes().length != parameters.length)
            return false;

        for (int i = 0; i < parameters.length; i++)
        {
            if (!changeSetMethod.getParameterTypes()[i].equals(parameters[i]))
                return false;
        }

        return true;
    }


    private void validateConfig() throws MigMongConfigurationException
    {
        if (!hasText(dbName))
        {
            throw new MigMongConfigurationException(
                    "DB name is not set. It should be defined in MongoDB URI or via setter");
        }
        if (!hasText(changeLogsScanPackage))
        {
            throw new MigMongConfigurationException("Scan package for change logs is not set: use appropriate setter");
        }
    }


    private ChangeEntryDao createDao()
    {
        return new ChangeEntryDao(DEFAULT_CHANGELOG_COLLECTION_NAME, DEFAULT_LOCK_COLLECTION_NAME,
                DEFAULT_WAIT_FOR_LOCK,
                DEFAULT_CHANGE_LOG_LOCK_WAIT_TIME, DEFAULT_CHANGE_LOG_LOCK_POLL_RATE,
                DEFAULT_THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);
    }
}

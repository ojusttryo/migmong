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

import com.github.ojusttryo.migmong.exception.MigrationConfigurationException;
import com.github.ojusttryo.migmong.exception.MigrationConnectionException;
import com.github.ojusttryo.migmong.exception.MigrationException;
import com.github.ojusttryo.migmong.exception.MigrationLockException;
import com.github.ojusttryo.migmong.migration.MigrationInfo;
import com.github.ojusttryo.migmong.migration.MigrationContext;
import com.github.ojusttryo.migmong.dao.ChangeEntryDao;
import com.github.ojusttryo.migmong.migration.MigrationEntry;
import com.github.ojusttryo.migmong.migration.Version;
import com.github.ojusttryo.migmong.migration.annotations.Migration;
import com.github.ojusttryo.migmong.utils.MigrationService;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ReadConcern;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;

/**
 * Mongo migration entry point
 */
public class MongoMigration implements InitializingBean
{
    private static final Logger logger = LoggerFactory.getLogger(MongoMigration.class);

    private static final String DEFAULT_MIGRATION_COLLECTION_NAME = "migration_log";
    private static final String DEFAULT_LOCK_COLLECTION_NAME = "migration_lock";
    private static final boolean DEFAULT_WAIT_FOR_LOCK = false;
    private static final long DEFAULT_MIGRATION_LOCK_WAIT_TIME = 5L;
    private static final long DEFAULT_MIGRATION_LOCK_POLL_RATE = 10L;
    private static final boolean DEFAULT_THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK = false;

    private ChangeEntryDao dao;

    private boolean enabled = true;
    private String migrationScanPackage;
    private MongoClientURI mongoClientURI;
    private String dbName;

    private MongoClient mongoClient;
    private MigrationContext migrationContext = new MigrationContext();
    private Version applicationVersion = new Version();
    private String migrationPrefix = "V";


    /**
     * Simple constructor with default configuration of host (localhost) and port (27017).
     * The database name need to be provided using {@link MongoMigration#setDbName(String)} setter.
     * It is recommended to use constructors with MongoURI
     */
    public MongoMigration()
    {
        this(new MongoClientURI("mongodb://" + defaultHost() + ":" + defaultPort() + "/"));
    }


    /**
     * Constructor takes {@link MongoClientURI} object as a parameter.
     * @param mongoClientURI URI for the database
     */
    public MongoMigration(MongoClientURI mongoClientURI)
    {
        this.mongoClientURI = mongoClientURI;
        this.setDbName(mongoClientURI.getDatabase());
        this.dao = createDao();
    }


    /**
     * Constructor takes {@link MongoClient} object as a parameter.
     * @param mongoClient database connection client
     */
    public MongoMigration(MongoClient mongoClient)
    {
        this.mongoClient = mongoClient;
        this.dao = createDao();
    }


    /**
     * Constructor takes correct uri string for {@link MongoClientURI}
     * <p>The format of the URI is:
     * <pre>
     * mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]]
     * [/[database[.collection]][?options]]
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
     * <b>MongoMigration will operate on the database provided here or on the database overriden by setter setDbName(String)
     * .</b>
     * </li>
     * <li>{@code ?options} are connection options. For list of options please see com.mongodb.MongoClientURI docs</li>
     * </ul>
     * @param mongoURI with correct format
     */
    public MongoMigration(String mongoURI)
    {
        this(new MongoClientURI(mongoURI));
    }


    /**
     * Constructor with separate connection parameters
     * @param host MongoDB host, e.g. '127.0.0.1'
     * @param port MongoDB port (the default for MongoDB is 27017)
     * @param dbName database name
     * @param user user name
     * @param password user password
     */
    public MongoMigration(String host, Integer port, String dbName, String user, String password)
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
     * For Spring users: executing migration after bean is created in the Spring context
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        execute();
    }


    /**
     * Closes the Mongo instance used by MongoMigration.
     * This will close either the connection MongoMigration was initiated with or that which was internally created.
     */
    public void close()
    {
        dao.close();
    }


    /**
     * Executing migration. Executes only {@link Migration Migrations} which version are less or equals to current application version
     * and ignores already executed.
     */
    public void execute() throws MigrationException
    {
        if (!isEnabled())
        {
            logger.info("MongoMigration is disabled. Exiting.");
            return;
        }

        validateConfig();

        if (this.mongoClient != null)
            dao.connectMongoDb(this.mongoClient, dbName);
        else
            dao.connectMongoDb(this.mongoClientURI, dbName);

        if (!dao.acquireProcessLock())
        {
            logger.warn("MongoMigration did not acquire process lock. Exiting.");
            return;
        }

        logger.info("MongoMigration acquired process lock, starting the data migration sequence..");

        try
        {
            executeMigration();
        }
        finally
        {
            logger.info("MongoMigration is releasing process lock.");
            dao.releaseProcessLock();
        }

        logger.info("MongoMigration has finished his job.");
    }


    /**
     * Sets application version to limit migrations. Migrations higher than this version are ignored.
     * Required
     * @param version version of application
     */
    public void setApplicationVersion(Version version)
    {
        this.applicationVersion = version;
    }


    /**
     * Sets prefix for migration names. Default is 'V'.
     * <p>Examples:</p>
     * <ul>
     * <li>Migration V1_1_1__something has prefix 'V'</li>
     * <li>Migration V_1_2__doStuff has prefix 'V_'</li>
     * <li>etc.</li>
     * </ul>
     * @param prefix prefix for migration name
     */
    public void setMigrationNamePrefix(String prefix)
    {
        this.migrationPrefix = prefix;
    }


    /**
     * Sets application version to limit migration. Migrations higher than this version are ignored.
     * @param version version. Might contains from 1 to 3 numbers (major, minor, build)
     * @param delimiter delimiter for numbers in version
     */
    public void setApplicationVersion(String version, String delimiter)
    {
        this.applicationVersion = Version.from(version, delimiter);
    }


    /**
     * @return true if MongoMigration runner is enabled and able to run, otherwise false
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    /**
     * @return true if an execution is in progress, in any process.
     */
    public boolean isExecutionInProgress() throws MigrationConnectionException
    {
        return dao.isProccessLockHeld();
    }


    /**
     * Poll rate for acquiring lock if waitForLock is true
     * @param migrationLockPollRate Poll rate in seconds for acquiring lock
     */
    public MongoMigration setMigrationLockPollRate(long migrationLockPollRate)
    {
        this.dao.setMigrationLockPollRate(migrationLockPollRate);
        return this;
    }


    /**
     * Waiting time for acquiring lock if waitForLock is true
     * @param migrationLockWaitTime Waiting time in minutes for acquiring lock
     */
    public MongoMigration setMigrationLockWaitTime(long migrationLockWaitTime)
    {
        this.dao.setMigrationLockWaitTime(migrationLockWaitTime);
        return this;
    }


    /**
     * Package name where {@link Migration}-annotated classes are kept.
     * @param migrationScanPackage package where your migrations are
     */
    public MongoMigration setMigrationScanPackage(String migrationScanPackage)
    {
        this.migrationScanPackage = migrationScanPackage;
        return this;
    }


    /**
     * Overwrites a default name of collection for migration log instead of {@link #DEFAULT_MIGRATION_COLLECTION_NAME}
     * <p>CAUTION! Use this method carefully - when changing the name on a existing system,
     * your migrations will be executed again on your MongoDB instance</p>
     * @param migrationCollectionName a new migration collection name
     */
    public MongoMigration setMigrationCollectionName(String migrationCollectionName)
    {
        this.dao.setMigrationCollectionName(migrationCollectionName);
        return this;
    }


    /**
     * Used DB name should be set here or via MongoDB URI (in a constructor)
     * @param dbName database name
     */
    public MongoMigration setDbName(String dbName)
    {
        this.dbName = dbName;
        return this;
    }


    /**
     * Feature which enables/disables MongoMigration runner execution
     * @param enabled {@link MongoMigration} will run only if this option is set to true
     */
    public MongoMigration setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }


    /**
     * Overwrites a default MongoMigration lock collection hardcoded in {@link #DEFAULT_LOCK_COLLECTION_NAME}
     * @param lockCollectionName a new lock collection name
     */
    public MongoMigration setLockCollectionName(String lockCollectionName)
    {
        this.dao.setLockCollectionName(lockCollectionName);
        return this;
    }


    /**
     * Sets pre-configured {@link MongoTemplate} instance to use in migration
     * @param mongoTemplate instance of the {@link MongoTemplate}
     */
    public MongoMigration setMongoTemplate(MongoTemplate mongoTemplate)
    {
        migrationContext.setMongoTemplate(mongoTemplate);
        return this;
    }


    /**
     * Set Spring's {@link Environment} object to use in migration
     * @param environment {@link Environment} object
     */
    public MongoMigration setSpringEnvironment(Environment environment)
    {
        migrationContext.setSpringEnvironment(environment);
        return this;
    }


    /**
     * Sets Spring's {@link ApplicationContext} object to use in migration
     * @param applicationContext application context
     */
    public MongoMigration setApplicationContext(ApplicationContext applicationContext)
    {
        migrationContext.setApplicationContext(applicationContext);
        return this;
    }


    /**
     * Sets custom variable to use in migration
     * @param name variable name
     * @param variable variable
     */
    public MongoMigration setCustomVariable(String name, Object variable)
    {
        migrationContext.setVariable(name, variable);
        return this;
    }


    /**
     * Feature which enables/disables throwing {@link MigrationLockException} if migration can not obtain lock
     * @param throwException should throw {@link MigrationLockException} if lock can not be obtained
     */
    public MongoMigration setThrowExceptionIfCannotObtainLock(boolean throwException)
    {
        this.dao.setThrowExceptionIfCannotObtainLock(throwException);
        return this;
    }


    /**
     * Feature which enables/disables waiting for lock if it's already obtained
     * @param waitForLock should wait for lock
     */
    public MongoMigration setWaitForLock(boolean waitForLock)
    {
        this.dao.setWaitForLock(waitForLock);
        return this;
    }


    private void executeMigration() throws MigrationException
    {
        MigrationService service = new MigrationService(migrationScanPackage);

        for (MigrationInfo migrationInfo : service.fetchMigrations(applicationVersion, migrationPrefix))
        {
            try
            {
                Object migrationInstance = migrationInfo.getMigrationClass().getConstructor().newInstance();
                List<Method> migrationUnits = service.fetchMigrationUnits(migrationInstance.getClass());

                for (Method migrationUnit : migrationUnits)
                {
                    MigrationEntry migrationEntry = service.createMigrationEntry(migrationUnit);

                    try
                    {
                        if (dao.isNewMigrationUnit(migrationEntry))
                        {
                            executeMigrationUnit(migrationUnit, migrationInstance, dao.getMongoDatabase());
                            dao.save(migrationEntry);
                            logger.info(migrationEntry + " applied");
                        }
                        else if (service.isAlwaysRunnableMigration(migrationUnit))
                        {
                            executeMigrationUnit(migrationUnit, migrationInstance, dao.getMongoDatabase());
                            logger.info(migrationEntry + " applied");
                        }
                        else
                        {
                            logger.debug(migrationEntry + " passed over");
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage());
                    }
                }
            }
            catch (NoSuchMethodException | IllegalAccessException | InstantiationException e)
            {
                throw new MigrationException(e.getMessage(), e);
            }
            catch (InvocationTargetException e)
            {
                throw new MigrationException(e.getTargetException().getMessage(), e);
            }
        }
    }


    private Object executeMigrationUnit(Method migrationUnit, Object migration, MongoDatabase mongoDatabase)
            throws IllegalAccessException, InvocationTargetException
    {
        migrationContext.setMongoDatabase(mongoDatabase);
        return migrationUnit.invoke(migration, migrationContext);
    }


    private void validateConfig() throws MigrationConfigurationException
    {
        if (!hasText(dbName))
        {
            throw new MigrationConfigurationException(
                    "DB name is not set. It should be defined in MongoDB URI or via setter");
        }
        if (!hasText(migrationScanPackage))
        {
            throw new MigrationConfigurationException("Scan package for migrations is not set: use appropriate setter");
        }
    }


    private ChangeEntryDao createDao()
    {
        return new ChangeEntryDao(DEFAULT_MIGRATION_COLLECTION_NAME, DEFAULT_LOCK_COLLECTION_NAME,
                DEFAULT_WAIT_FOR_LOCK,
                DEFAULT_MIGRATION_LOCK_WAIT_TIME, DEFAULT_MIGRATION_LOCK_POLL_RATE,
                DEFAULT_THROW_EXCEPTION_IF_CANNOT_OBTAIN_LOCK);
    }
}

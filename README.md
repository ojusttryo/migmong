
## Introduction

**migmong** is a Java tool which helps you to *manage changes* in your MongoDB.
The concept is very similar to other db migration tools such as [Flyway](http://flywaydb.org) but *without using XML/JSON/YML files*.

The goal is to keep this tool simple and comfortable to use.


**migmong** provides an approach for adding changes based on Java classes and methods with appropriate annotations.

## Getting started

### Add a dependency (not working yet)

With Maven
```xml
<dependency>
    <groupId>com.github.migmong</groupId>
    <artifactId>migmong</artifactId>
    <version>1.0</version>
</dependency>
```


### Usage with Spring

You need to instantiate `MongoMigration` object and provide some configuration.
If you use Spring can be instantiated as a singleton bean in the Spring context. 
In this case the migration process will be executed automatically on startup.

```java
@Bean
public MongoMigration mongoMigration()
{
    MongoMigration migration = new MongoMigration(host, port, name, user, password);
    migration.setMigrationScanPackage("com.foo.database.migrations");
    return migration;
}
```


### Usage without Spring
Using **migmong** without a spring context has similar configuration but you have to remember to run `execute()` method to start a migrationInfo process.

```java
MongoMigration migration = new MongoMigration(host, port, name, user, password);
migration.setMigrationScanPackage("com.foo.database.migrations");
migration.execute();
```

Above examples provide minimal configuration. `MongoMigration` object provides some other possibilities (setters) to make the tool more flexible:

```java
migration.setMigrationCollectionName("migrationLog");   // collection with applied change sets
migration.setMigrationNamePrefix("V_");                 // prefix of names of all migrations, default is 'V'
migration.setApplicationContext(context);               // instance of Spring application context to get your beans
migration.setMongoTemplate(mongoTemplate);              // instance of Spring MongoTemplate
migration.setSpringEnvironment(environment);            // instance of Spring Environment
migration.setCustomVariable(name, variable);            // any variable you want to have during the migration process
migration.setEnabled(true);                             // default is true, migrationInfo won't start if set to false
```

You can specify connection options in several ways: `MongoClientURI`, `MongoClient`, mongo URI string, or options like in the above examples (host, port, dbName, user, password)


MongoDB URI format:
```
mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database[.collection]][?options]]
```
[More about URI](http://mongodb.github.io/mongo-java-driver/3.5/javadoc/)


### Creating migrations

`Migration` contains bunch of `MigrationUnit`. `MigrationUnit` is a single task (set of instructions made on a database). In other words `Migration` is a class annotated with `@Migration` and containing methods annotated with `@MigrationUnit`.

```java 
package com.foo.database.migrations;

@Migration
public class V0_1_0__migrateMyStuff
{
    @MigrationUnit(id = 1)
    public void makeMyBigChanges(MigrationContext context)
    {
        // get your information that was put into the migration context
        MyBean myBean = context.getApplicationContext().getBean(MyBean.class);
        ...
        // do some stuff
        ... 
    }

    @MigrationUnit(id = 2)
    public void makeEvenBiggerChanges(MigrationContext context)
    {
        
    }
}
```

#### @Migration

Class with migration units must be annotated by `@Migration`. Migrations are sorted by the version, that you specify at the beginning of each class name after prefix.
```java
@Migration
public class V0_1_0__migrateMyStuff
{
    
}
```
In this case:  
`V` is prefix  
`0_1_0` is version  
`__` - separator between version and migration name  
`migrateMyStuff` - migration name

#### Migration version

You can specify either version you like using numbers and separator `_`. For example: `1`, `2_3_57`, etc.

#### @MigrationUnit

Method annotated by `@MigrationUnit` is taken and applied to the database. History of applied migration units is stored in a migration log collection (by default 'migrationLog') in your MongoDB

```java
@MigrationUnit(id = 5)
public void foo(MigrationContext context)
{
    
}
```
Each method must take the `MigrationContext` parameter, which contains all the data that you passed there during initialization.

`id` - a number of the migration unit. This attribute is required and should be unique. It is used to sort your methods.

## Known issues

##### Mongo java driver conflicts

**migmong** depends on `mongo-java-driver`. If your application has mongo-java-driver dependency too, there could be a library conflicts in some cases.

**Workaround**:

You can exclude mongo-java-driver from **migmong** and use your dependency only. 

## Current state of library

For now the library **is not put to any repository**. Also **it is not well tested**. It should be used carefully.

So you can download source code and compile the library. Also you can specify your own library versions in the pom.xml.

To use it within your project you can include it as .jar file and add in pom.xml this way:

```xml
<dependency>
    <groupId>com.github.migmong</groupId>
    <artifactId>migmong</artifactId>
    <version>1.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/src/main/resources/migmong.jar</systemPath>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.mongodb</groupId>
            <artifactId>mongo-java-driver</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```
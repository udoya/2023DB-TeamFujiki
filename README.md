# 2023DB-TeamFujiki

## Required
You have to prepare these applications or libraries.
- [Cassandra](https://cassandra.apache.org/_/index.html)
- React
- [Gradle](https://gradle.org/)

## Server
We use Cassandra. Prepare and Launch Cassandra DB before the first run.

## How to start
After clone this Project, move to "server" directory.
```
cd ./server
```

Only the first run, you should create scheme by scalarDB, and load the data.
```
java -jar scalardb-schema-loader-3.9.1.jar --config scalardb.properties --schema-file schema.json --coordinator
```
```
gradle run --args="load"
```

After that, activate the server side.
```
gradle run
```

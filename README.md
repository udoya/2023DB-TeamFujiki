# 2023DB-TeamFujiki

## Required
You have to prepare these applications or libraries.
- [Cassandra](https://cassandra.apache.org/_/index.html)
- [Node.js](https://nodejs.org/ja)
- [npm](https://www.npmjs.com/)
- [React](https://ja.legacy.reactjs.org/)
- [Gradle](https://gradle.org/)

## Client
We use React as the front end. Therefore, first, make sure that Node.js and npm are installed.

## Server
We use Cassandra. Prepare and Launch Cassandra DB before the first run.

## How to start(client)
After cloning this Project, move to the "client" directory.
```
cd ./client
```

First, you can install the package using npm. (There is a problem with the version of material-ui and you need to add --force.)
```
npm install --force
```

To run the client in a browser (localhost:3000), simply type the following command.
```
npm start
```

## How to start(server)
After cloning this Project, move to the "server" directory.
```
cd ./server
```

Only the first run, you should create a scheme by scalarDB, and load the data.
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

If you are able to run both the client and server, you can log in at the localhost:3000 login page with one of the following usernames: 
1. John
1. Bob
1. Emma

Once logged in, you can sell your own items in order of earliest to latest, and participants other than the seller can bid on them.

# 2023DB-TeamFujiki

##Server
We use Cassandra



~~~
cd ./server

java -jar scalardb-schema-loader-3.9.1.jar --config scalardb.properties --schema-file schema.json --coordinator

//load initial data for the first run
gradle run --args="load"

gradle run


~~~

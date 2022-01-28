# Running the code and sample input.

A simple java code using DynamoDB API but utilizing the alternator feature of Scylla DB.

Can be executed as a standalone node/as cluster.
For a local executor set the developer mode to 1. Else set to 0 and execute the tests. 

Sample json input for the load test using CURL 

curl -X POST  -H"Content-Type: application/json;charset=UTF-8" -H"Accept: application/json;charset=UTF-8"  --data @./input.json "http://localhost:8080/data/load"

{
"tableName":"poc",
"limit":50000, 
"enableStream": true, <= Creates the CDC logs for equivalent.
"useCluster":false, <= If set to true uses the AlternatorLoadBalancing API.              
"id":1 <= If not set the partition key lies between 1 and 4. picked randomly.
}


To retrieve the time taken to fetch the data for given parition key.

curl -X POST  -H"Content-Type: application/json;charset=UTF-8" -H"Accept: application/json;charset=UTF-8"  --data @./data.json "http://localhost:8080/data"

{
"tableName":"poc",
 "id": 1
}

To print the streams data. 

curl  "http://localhost:8080/data/streams?tableName=poc" 



# Running the code and sample input .

It is strictly a Proof of Concept setup for scylla using dynamo DB API with few performance validations.

Java code using DynamoDB API but utilizing the alternator feature of Scylla DB.

Can be executed as a standalone or multi node cluster.
For a local executor set the developer mode to 1. Else set to 0 and execute the tests. 

Run the maven build and use the docker configuration for a multi node cluster setup.

The tables are created and then loaded.

Sample json input for the load test using CURL 

curl -X POST  -H"Content-Type: application/json;charset=UTF-8" -H"Accept: application/json;charset=UTF-8"  --data @./input.json "http://localhost:8080/data/load"

{
"tableName":"poc",
"limit":50000, 
"dbType": "scylla" , <= "scylla"/"dynamo"
"enableStream": true, <= Creates the CDC logs for equivalent.
"useCluster":false, <= If set to true uses the AlternatorLoadBalancing API.              
"id":1 <= If not set the partition key lies between 1 and 4. picked randomly.
}


To retrieve the time taken to fetch the data for given parition key.

curl -X POST  -H"Content-Type: application/json;charset=UTF-8" -H"Accept: application/json;charset=UTF-8"  --data @./data.json "http://localhost:8080/data"

{
"tableName":"poc",
"dbType": "scylla" , <= "scylla"/"dynamo"
 "id": 1
}

To look through the streams using the DynamoDB API. This is only the logger and tweak this as per the test case if needed

curl  "http://localhost:8080/streams?tableName=poc&dbType=scylla" 



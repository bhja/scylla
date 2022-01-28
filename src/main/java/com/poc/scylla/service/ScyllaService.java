package com.poc.scylla.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreams;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreamsClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.model.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.scylla.model.DataRequest;
import com.poc.scylla.model.InsertRequest;
import com.poc.scylla.model.Row;
import com.scylladb.alternator.AlternatorRequestHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StopWatch;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Log4j2
public class ScyllaService {

    private Executor executor;
    private String host;
    private int alternatorPort;
    private int nativePort = 9042;
    private boolean enableNativePort;
    private DynamoDB clusterClient;
    private DynamoDB standaloneClient;
    private ObjectMapper objectMapper = new ObjectMapper();
    private AmazonDynamoDB dbClient;
    private AmazonDynamoDBStreams streamsClient;


    public ScyllaService(Executor pExecutor,String pHost,int pAlternatorPort,boolean pEnableNativePort){
            enableNativePort = pEnableNativePort;
            executor = pExecutor;
            host = pHost;
            alternatorPort = pAlternatorPort;
    }

    protected DynamoDB getClusterClient(){
        if(clusterClient == null){
            //POC ONLY . NOT THE WAY TO DO.
            URI node = URI.create(String.format("http://%s:%s",host,alternatorPort));
            AlternatorRequestHandler handler =
                    new AlternatorRequestHandler(node);
            log.info("Handler initialized");

            AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion("None")
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                            "None","None"
                    ))).withRequestHandlers(handler).build();
            clusterClient = new DynamoDB(dynamoDB);
        }

        return clusterClient;
    }

    protected DynamoDB getNodeClient() {
        if (standaloneClient == null) {
            AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.
                        EndpointConfiguration(String.format("http://%s:%s",host,alternatorPort), "None");

            dbClient = AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(endpoint).withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                            "None","None"
                    ))).build();
            standaloneClient = new DynamoDB(dbClient);

            streamsClient =
                    AmazonDynamoDBStreamsClientBuilder
                            .standard()
                            .withEndpointConfiguration(endpoint).withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                            "None","None"
                    ))).build();

        }
        return standaloneClient;
    }

    public ScyllaService(Executor pExecutor,String pHost,int pAlternatorPort){
            this(pExecutor,pHost,pAlternatorPort,false);
    }

    @Async
    public void load(InsertRequest insertRequest){
            log.info("Request to process [{}]", insertRequest);
            createTable(insertRequest);
            log.info("Loading  the table  [{}]", insertRequest);
            loadData(insertRequest);

    }

    protected void loadData(InsertRequest input){
        Table table ;
        if(input.isUseCluster()){
            table = getClusterClient().getTable(input.getTableName());
        }else {
            table = getNodeClient().getTable(input.getTableName());
        }
        long start = System.currentTimeMillis();
        for(int i = 1;i<=input.getLimit();i++){
            insert(i,start,table,input);
        }
    }

    protected void insert(final int rowCnt,final long start,Table table,InsertRequest req){
        int random = (int) (Math.random() * (4 - 1)) + 1;
        Row row = new Row(random,String.format("test-row-%s",rowCnt));
        if(req.isSingleThread()){
            try {
                table.putItem(new Item().withPrimaryKey("id", req.getId()==0 ? random:req.getId())
                        .withString("jsonData", objectMapper.writeValueAsString(row))
                        .withString("createTimestamp", LocalDateTime.now().toString()));
            } catch (Exception e) {
                log.error("error writing to table {} ", e);
            }
            if (rowCnt % 10000 == 0) {
                log.info("Time taken to load {} so far {} ms /{} minutes", rowCnt, System.currentTimeMillis() - start, (System.currentTimeMillis() - start) / 60000);
            }
            if (rowCnt == req.getLimit()) {
                long end = System.currentTimeMillis() - start;
                log.info("Request [{}] statistics - total time to load {}  is records  {} ms or {} minutes",
                        req,req.getLimit(),end, end / 60000);
            }
        }else {
            CompletableFuture.runAsync(() -> {
                try {
                    table.putItem(new Item().withPrimaryKey("id", req.getId() == 0 ? random:req.getId())
                            .withString("jsonData", objectMapper.writeValueAsString(row))
                            .withString("createTimestamp", LocalDateTime.now().toString()));
                } catch (Exception e) {
                    log.error("error writing to table ", e);
                }
            }, executor).whenComplete((v, e) -> {
                long end = System.currentTimeMillis() - start;
                if (rowCnt % 10000 == 0) {
                    log.info("Time taken to load {} so far {} ms /{} minutes", rowCnt, System.currentTimeMillis() - start, (System.currentTimeMillis() - start) / 60000);
                }
                if (rowCnt == req.getLimit()) {
                    log.info("Request [{}] statistics - total time to load {}  is records  {} ms or {} minutes",
                            req,req.getLimit(),end, end / 60000);
                }
            });
        }
    }

    protected void createTable(InsertRequest insertRequest){
        try {
            DynamoDB client = insertRequest.isUseCluster() ? getClusterClient():getNodeClient();
            List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            attributeDefinitions.add(new AttributeDefinition().withAttributeName("id").withAttributeType("N"));
            attributeDefinitions.add(new AttributeDefinition().withAttributeName("jsonData").withAttributeType("S"));
            attributeDefinitions.add(new AttributeDefinition().withAttributeName("createTimestamp").withAttributeType("S"));

            List<KeySchemaElement> keySchema = new ArrayList<>();
            keySchema.add(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH));
            keySchema.add(new KeySchemaElement().withAttributeName("createTimestamp").withKeyType(KeyType.RANGE));
            CreateTableRequest request = new CreateTableRequest()
                    .withTableName(insertRequest.getTableName())
                    .withKeySchema(keySchema)
                    .withAttributeDefinitions(attributeDefinitions)
                    .withProvisionedThroughput(new ProvisionedThroughput()
                            .withReadCapacityUnits(5L)
                            .withWriteCapacityUnits(6L));

            if(insertRequest.isEnableStream()){
                StreamSpecification streamSpecification = new StreamSpecification()
                        .withStreamEnabled(true)
                        .withStreamViewType(StreamViewType.NEW_AND_OLD_IMAGES);
                request.setStreamSpecification(streamSpecification);
            }
            Table table = client.createTable(request);
            table.waitForActive();

        }catch (Exception e){
            log.error("Issue creating the table ==> \n [{}] ",e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public List getRecords(DataRequest request) {
            Table table = request.isUseCluster() ? getClusterClient().getTable(request.getTableName()) : getNodeClient().getTable(request.getTableName());
            QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("id = :id")
                .withValueMap(new ValueMap().withNumber(":id",request.getId()));
            List<Map<String,String>> results = new ArrayList<>();
            StopWatch watch = new StopWatch();
            try {
                watch.start();
                ItemCollection<QueryOutcome> items = table.query(spec);
                Iterator<Item> iterator = items.iterator();
                int count=0;

                while (iterator.hasNext()) {
                    Item item = iterator.next();
                    count = count + 1;
                    log.info("{}",item);
                }
                watch.stop();
                results.add(Collections.singletonMap("total_count",""+count));
                results.add(Collections.singletonMap("time_taken",watch.getTotalTimeMillis() + " ms/" +watch.getTotalTimeNanos() + " ns"));
                log.info("Time taken to read the data  for id {} is {} nanosecs/{} ms ", request.getId(),watch.getTotalTimeNanos(),watch.getTotalTimeMillis());
            }catch (Exception e){
                log.error("Could not fetch the data {} ",e.getMessage());
            }
            return results;
        }

        public void streams(String table){

            getNodeClient();
            DescribeTableResult describeTableResult =  dbClient.describeTable(table);
            String streamArn = describeTableResult.getTable().getLatestStreamArn();

            String lastEvaluatedShardId = null;

            do {
                DescribeStreamResult describeStreamResult = streamsClient.describeStream(
                        new DescribeStreamRequest()
                                .withStreamArn(streamArn)
                                .withExclusiveStartShardId(lastEvaluatedShardId));
                List<Shard> shards = describeStreamResult.getStreamDescription().getShards();

                for (Shard shard : shards) {
                    String shardId = shard.getShardId();
                    GetShardIteratorRequest getShardIteratorRequest = new GetShardIteratorRequest()
                            .withStreamArn(streamArn)
                            .withShardId(shardId)
                            .withShardIteratorType(ShardIteratorType.TRIM_HORIZON);
                    GetShardIteratorResult getShardIteratorResult =
                            streamsClient.getShardIterator(getShardIteratorRequest);
                    String currentShardIter = getShardIteratorResult.getShardIterator();
                    int processedRecordCount = 0;
                    //JUST HARDCODED 10. This is for the POC.
                    while (currentShardIter != null && processedRecordCount < 10) {
                        GetRecordsResult getRecordsResult = streamsClient.getRecords(new GetRecordsRequest()
                                .withShardIterator(currentShardIter));
                        List<Record> records = getRecordsResult.getRecords();
                        if(records.size() > 0){
                            for (Record record : records) {
                                log.info("{}",record.getDynamodb());
                            }
                        }
                        processedRecordCount += 1;
                        currentShardIter = getRecordsResult.getNextShardIterator();
                    }
                }

                // If LastEvaluatedShardId is set, then there is
                // at least one more page of shard IDs to retrieve
                lastEvaluatedShardId = describeStreamResult.getStreamDescription().getLastEvaluatedShardId();

            } while (lastEvaluatedShardId != null);
        }

}








package com.poc.scylla.service;

import com.poc.scylla.model.InsertRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Executor;

@Slf4j
public class DynamoDBService extends AbstractService{

    private String region;
    private String accessKey;
    private String secretKey;
    public DynamoDBService(Executor executor,String pRegion,String pAccessKey,String pSecretKey){
        super(executor);
        region = pRegion;
        accessKey = pAccessKey;
        secretKey = pSecretKey;
    }

    @Override String getHost() {
        return null;
    }

    @Override int getAlternatorPort() {
        return 0;
    }

    @Override
    String getRegion(){
        return region;
    }

    @Override String getAccessKey() {
        return accessKey;
    }

    @Override String getSecretKey() {
        return secretKey;
    }

    @Async
    public void load(InsertRequest insertRequest){
        super.load(insertRequest);
    }




}

package com.poc.scylla.service;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.poc.scylla.model.InsertRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Executor;

@Log4j2
public class ScyllaService extends AbstractService{

    private String host;
    private int alternatorPort;
    private boolean enableNativePort;

    public ScyllaService(Executor pExecutor,String pHost,int pAlternatorPort,boolean pEnableNativePort){
        super(pExecutor);
            enableNativePort = pEnableNativePort;
            host = pHost;
            alternatorPort = pAlternatorPort;
    }


    public ScyllaService(Executor pExecutor,String pHost,int pAlternatorPort){
        this(pExecutor,pHost,pAlternatorPort,false);
    }

    @Async
    public void load(InsertRequest insertRequest){
            super.load(insertRequest);

    }

    protected void loadData(InsertRequest input){
        Table table ;
        if(input.isUseCluster()){
            table = getClusterClient().getTable(input.getTableName());
        }else {
            table = getClient(input.getDbType()).getTable(input.getTableName());
        }
        long start = System.currentTimeMillis();
        for(int i = 1;i<=input.getLimit();i++){
            insert(i,start,table,input);
        }
    }

    @Override String getHost() {
        return host;
    }

    @Override
    int getAlternatorPort() {
        return alternatorPort;
    }

    @Override String getRegion() {
        return null;
    }

    @Override String getAccessKey() {
        return null;
    }

    @Override String getSecretKey() {
        return null;
    }
}








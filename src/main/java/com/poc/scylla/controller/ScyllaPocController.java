package com.poc.scylla.controller;

import com.poc.scylla.model.DataRequest;
import com.poc.scylla.model.InsertRequest;
import com.poc.scylla.service.DynamoDBService;
import com.poc.scylla.service.ScyllaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ScyllaPocController {

    @Autowired
    ScyllaService service;

    @Autowired
    DynamoDBService dbService;

    @PostMapping
            (
            value = "/data",
            consumes =  MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List getRecords(@RequestBody DataRequest request) {
        if (request.getDbType().equals("scylla")) {
            return service.getRecords(request);
        } else if (request.getDbType().equals("dynamo")) {
            return dbService.getRecords(request);
        }else{
            return new ArrayList();
        }
    }



    @PostMapping(value="/data/load",
            consumes =  MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public void insertRecord(@RequestBody InsertRequest insertRequest){
        if(insertRequest.getDbType().equals("scylla")) {
            service.load(insertRequest);
        }else if(insertRequest.getDbType().equals("dynamo")){
            dbService.load(insertRequest);
        }
    }

    @GetMapping("/streams")
    public void getStreams(@RequestParam String tableName,@RequestParam String dbType){
        if(dbType.equals("scylla")) {
            service.streams(tableName, dbType);
        }else {
            dbService.streams(tableName,dbType);
        }
    }

}

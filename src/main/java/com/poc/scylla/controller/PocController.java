package com.poc.scylla.controller;

import com.poc.scylla.model.DataRequest;
import com.poc.scylla.model.InsertRequest;
import com.poc.scylla.service.ScyllaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PocController {

    @Autowired
    ScyllaService service;

    @PostMapping
            (
            value = "/data",
            consumes =  MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List getRecords(@RequestBody DataRequest request){
       return   service.getRecords(request);

    }

    @PostMapping(value="/data/load",
            consumes =  MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public void insertRecord(@RequestBody InsertRequest insertRequest){
        service.load(insertRequest);
    }

    @GetMapping("/streams")
    public void getStreams(@RequestParam String tableName){
        service.streams(tableName);
    }

}

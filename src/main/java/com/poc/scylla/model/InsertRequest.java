package com.poc.scylla.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertRequest {
    String tableName;
    int limit;
    boolean enableStream;
    boolean useCluster;
    boolean singleThread;
    int id;
}

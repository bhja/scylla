package com.poc.scylla.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataRequest {
    String dbType;
    String tableName;
    int limit;
    boolean useCluster;
    int id;
}

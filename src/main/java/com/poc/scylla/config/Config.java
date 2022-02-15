package com.poc.scylla.config;

import com.amazonaws.util.StringUtils;
import com.poc.scylla.service.DynamoDBService;
import com.poc.scylla.service.ScyllaService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class Config {

    /**
     * The host name should be the one that it should connect to in docker
     * @param executor
     * @param host
     * @param alternatorPort
     * @param enableNativePort
     * @return
     */
    @Bean
    public ScyllaService allocateScyllaService(@Qualifier(value = "ScyllaDBExecutor") Executor executor,
                                               final @Value("${poc.service.host:scylla}")String host,
                                               final @Value("${poc.service.alternatorPort:8000}")int alternatorPort,
                                               final @Value("${poc.service.enableNativePort:false}") boolean enableNativePort){
        if(enableNativePort){
            return new ScyllaService(executor,host,alternatorPort,enableNativePort);
        }
        return new ScyllaService(executor,host,alternatorPort);

    }

    @Bean
    public DynamoDBService allocateDynamoDBService(@Qualifier(value = "DynamoDBExecutor") Executor executor,
    @Value("${scylladb.poc.aws.accessKey}") String accessKey,
    @Value("${scylladb.poc.aws.secretKey}")String secretKey,
    @Value("${scylladb.poc.aws.region}") String region
    )
    {
        if(StringUtils.isNullOrEmpty(accessKey)){
            accessKey = System.getenv("AWS_ACCESS_KEY");
        }
        if(StringUtils.isNullOrEmpty(secretKey)){
            secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        }
        if(StringUtils.isNullOrEmpty(region)){
            region = System.getenv("AWS_REGION");
        }
        if(StringUtils.isNullOrEmpty(accessKey) || StringUtils.isNullOrEmpty(region) || StringUtils.isNullOrEmpty(secretKey)){
            throw new RuntimeException("Cannot proceed without aws credentials and region specified either in config or as enviornment variable");
        }
        return new DynamoDBService(executor,region,accessKey,secretKey);
    }

    @Bean(name="ScyllaDBExecutor")
    public ThreadPoolTaskExecutor allocateThreadPool(
            final @Value("${poc.service.corePoolSize:1}") int corePoolSize,
            final @Value("${poc.service.maxPoolSize:1}") int maxPoolSize,
            final @Value("${poc.service.queueCapacity:2000000}") int queueCapacity,
            final @Value("${poc.service.threadPrefix:ScyllaExecutor}") String threadPrefix
    ){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() ;
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadPrefix);
        return executor;
    }

    @Bean(name="DynamoDBExecutor")
    public ThreadPoolTaskExecutor allocateDynamoDBThread(
            final @Value("${poc.service.corePoolSize:1}") int corePoolSize,
            final @Value("${poc.service.maxPoolSize:1}") int maxPoolSize,
            final @Value("${poc.service.queueCapacity:2000000}") int queueCapacity,
            final @Value("${poc.service.threadPrefix:DynamoExecutor}") String threadPrefix
    ){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() ;
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadPrefix);
        return executor;
    }
}

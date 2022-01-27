package com.poc.scylla.config;

import com.poc.scylla.service.ScyllaService;
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
    public ScyllaService allocateScyllaService(Executor executor,
                                               final @Value("${poc.service.host:scylla}")String host,
                                               final @Value("${poc.service.alternatorPort:8000}")int alternatorPort,
                                               final @Value("${poc.service.enableNativePort:false}") boolean enableNativePort){
        if(enableNativePort){
            return new ScyllaService(executor,host,alternatorPort,enableNativePort);
        }
        return new ScyllaService(executor,host,alternatorPort);

    }
    @Bean
    public ThreadPoolTaskExecutor allocateThreadPool(
            final @Value("${poc.service.corePoolSize:1}") int corePoolSize,
            final @Value("${poc.service.maxPoolSize:1}") int maxPoolSize,
            final @Value("${poc.service.queueCapacity:2000000}") int queueCapacity,
            final @Value("${poc.service.threadPrefix:PocExecutor}") String threadPrefix
    ){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() ;
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadPrefix);
        return executor;
    }
}

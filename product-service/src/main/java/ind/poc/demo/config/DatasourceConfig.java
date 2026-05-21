package ind.poc.demo.config;

import ind.poc.demo.database.DataSourceOperation;
import ind.poc.demo.database.DynamicRoutingDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DatasourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.primary")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }


    @Bean
    @ConfigurationProperties("spring.datasource.replica")
    public DataSourceProperties  replicaDataSourceProperties() {
        return new DataSourceProperties();
    }


    @Bean
    public DataSource writeDataSource(DataSourceProperties primaryDataSourceProperties) {
        return primaryDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public DataSource readDataSource(DataSourceProperties replicaDataSourceProperties) {
        return replicaDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean
    public DataSource routingDataSource(
            DataSource writeDataSource,
            DataSource readDataSource) {

        DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource();

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceOperation.WRITE, writeDataSource);
        targetDataSources.put(DataSourceOperation.READ, readDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(writeDataSource);

        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }



}




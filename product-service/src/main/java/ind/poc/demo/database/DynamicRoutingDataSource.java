package ind.poc.demo.database;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected DataSourceOperation determineCurrentLookupKey() {
        DataSourceOperation  key = DataSourceContextHolder.get();
        System.out.println("Routing to: " + key);
        return (key != null) ? key : DataSourceOperation.WRITE;

    }
}






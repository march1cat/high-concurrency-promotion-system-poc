package ind.poc.demo.config;

import ind.poc.demo.data.DeferredWaiter;
import ind.poc.demo.data.QueueMessage;
import ind.poc.demo.task.InventoryTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Configuration
public class WaitingPoolConfig {

    @Bean
    public ConcurrentHashMap<String, DeferredWaiter> waitingPool(){
        return new ConcurrentHashMap<>();
    }
    @Bean
    public ArrayBlockingQueue<InventoryTask> dataUpdatePool(){
        return new ArrayBlockingQueue<>(200);
    }

}

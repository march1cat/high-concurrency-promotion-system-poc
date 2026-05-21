package ind.poc.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ExecutorPoolConfig {

    @Bean(name = "cacheAsyncExecutor") // 為這個執行緒池取一個名字
    public Executor promoAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心執行緒數：常駐在池內的執行緒數量
        executor.setCorePoolSize(5);
        // 最大執行緒數：當隊列滿了之後，最高可以擴張到多少執行緒
        executor.setMaxPoolSize(10);
        // 隊列容量：等待執行的任務排隊空間
        executor.setQueueCapacity(100);
        // 執行緒名稱前綴：方便在 Log 中排查問題
        executor.setThreadNamePrefix("Cache-processor-");
        // 拒絕策略：當池子與隊列都滿了，新任務該怎麼辦
        // CallerRunsPolicy: 由「呼叫者所在的執行緒」執行該任務，通常是為了壓力回饋
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任務完成後再關閉（優雅關閉）
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    @Bean(name = "databaseAsyncExecutor") // 為這個執行緒池取一個名字
    public Executor databaseAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心執行緒數：常駐在池內的執行緒數量
        executor.setCorePoolSize(5);
        // 最大執行緒數：當隊列滿了之後，最高可以擴張到多少執行緒
        executor.setMaxPoolSize(10);
        // 隊列容量：等待執行的任務排隊空間
        executor.setQueueCapacity(100);
        // 執行緒名稱前綴：方便在 Log 中排查問題
        executor.setThreadNamePrefix("Database-processor-");
        // 拒絕策略：當池子與隊列都滿了，新任務該怎麼辦
        // CallerRunsPolicy: 由「呼叫者所在的執行緒」執行該任務，通常是為了壓力回饋
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任務完成後再關閉（優雅關閉）
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

}

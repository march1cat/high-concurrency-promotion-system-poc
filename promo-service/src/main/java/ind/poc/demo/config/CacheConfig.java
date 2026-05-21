package ind.poc.demo.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ind.poc.demo.data.PromotionItemRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {
    @Bean
    public Cache<String, List<PromotionItemRecord>> promotionCache() {
        return Caffeine.newBuilder()
                // 設定寫入後 10 分鐘過期
                .expireAfterWrite(10, TimeUnit.SECONDS)
                // 設定快取最大容量（避免記憶體溢出）
                .maximumSize(1000)
                .build();
    }
}

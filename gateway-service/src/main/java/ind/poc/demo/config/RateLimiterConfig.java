package ind.poc.demo.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
@Log4j2
public class RateLimiterConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String hostAddress = null;
            if (exchange.getRequest().getRemoteAddress() != null &&
                    exchange.getRequest().getRemoteAddress().getAddress() != null) {
                hostAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            }

            // 💡 關鍵優化：如果抓不到 IP（例如本地測試環境），給一個保底字串 "127.0.0.1"
            // 這樣 Redis 就能順利建立 "request_rate_limiter.{127.0.0.1}.tokens" 的 Key
            return Mono.justOrEmpty(hostAddress).defaultIfEmpty("127.0.0.1");
        };
    }
}

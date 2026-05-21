package ind.poc.demo.listener;

import com.github.benmanes.caffeine.cache.Cache;
import ind.poc.demo.data.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Log4j2
public class RedisReceiver {

    private final ConcurrentHashMap<String, DeferredWaiter> waitingPool;
    private final Cache<String, List<PromotionItemRecord>> localCache;
    public void onCompleteRefreshCurrentOn(RedisMessage<List<PromotionItemRecord>> redisMessage) {
        final String key = "collection:currentOn";
        localCache.invalidate(key);
        waitingPool.values().stream().filter(waiting -> waiting.getWaitType() == DeferredWaiter.WAIT_FOR.PROMOTED_ITEMS).forEach(waiting -> waiting.response(redisMessage.getData()));
    }
}
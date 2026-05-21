package ind.poc.demo.service;

import ind.poc.demo.data.*;
import ind.poc.demo.err.RedisInventoryNotExistException;
import ind.poc.demo.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CachePromotionService {

    private final RedisService redisService;
    private final DatabaseAccessService databaseAccessService;

    @Async("cacheAsyncExecutor")
    public void refreshStorage(String requestId, String itemId) {
        final String lockKey = "lock:storage:" + itemId;
        final boolean isGettingLock = redisService.tryLock(lockKey, requestId, 10L);
        if(isGettingLock) {
            try {
                String storageKey = "storage:" + itemId;
                final PromotedInventory itemInventory = databaseAccessService.getItemInventoryById(itemId);
                redisService.set(storageKey, itemInventory.getQuantity() - itemInventory.getFreezeQuantity(), 5000L);
                redisService.publish("on_complete_refresh_storage", RedisMessage.<String>builder().data(itemId).build());
            } finally {
                redisService.releaseLock(lockKey,requestId);
            }
        }
    }


    @Async("cacheAsyncExecutor")
    public void refreshCollectionCurrentPromoted(String requestId, String key) {
        final String lockKey = "lock:" + key;
            final boolean isGettingLock = redisService.tryLock(lockKey, requestId, 10L);
            if(isGettingLock) {
                try {
                    Object cachedData = redisService.get(key);
                    final List<PromotedItem> currentPromotedItems = databaseAccessService.getCurrentPromotedItems();
                    List<PromotedItem> promotedItems =  !currentPromotedItems.isEmpty() ? currentPromotedItems : List.of();
                    List<PromotionItemRecord> data = promotedItems.stream().map(item ->  PromotionItemRecord.builder()
                            .promotedItemId(item.getId())
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .isSoldOut(item.isSoldOut())
                            .build()).collect(Collectors.toList());
                    cachedData = CachedMessage.<List<PromotionItemRecord>>builder()
                            .expireTime(LocalDateTime.now().plusMinutes(3))
                            .data(data)
                            .build();
                    redisService.set(key, cachedData, 1200L);
                    redisService.publish("on_complete_refresh_current_promoted",
                            RedisMessage.<List<PromotionItemRecord>>builder().data(data)
                                    .build());
                } finally {
                    redisService.releaseLock(lockKey,requestId);
                }
            }
    }

    public boolean occupyInventory(String key, int quantity) {
        int result = redisService.executeInventoryDecrease(key, quantity);
        switch (result) {
            case -1:
                throw new RedisInventoryNotExistException();
            case 0:
                return false;
            default:
                return true;
        }
    }

    public boolean releaseInventory(String key, int quantity) {
        int result = redisService.executeInventoryIncrease(key, quantity);
        switch (result) {
            case -1:
                throw new RedisInventoryNotExistException();
            default:
                return true;
        }
    }






}

package ind.poc.demo.service;

import com.github.benmanes.caffeine.cache.Cache;
import ind.poc.demo.annotation.Idempotent;
import ind.poc.demo.data.*;
import ind.poc.demo.err.RedisInventoryNotExistException;
import ind.poc.demo.repository.PromotedInventoryRepository;
import ind.poc.demo.repository.PromotedProductsRepository;
import ind.poc.demo.task.InventoryTask;
import ind.poc.demo.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Log4j2
public class PromotionService {

    private final PromotedProductsRepository productsRepository;
    private final PromotedInventoryRepository promotedInventoryRepository;
    private final CachePromotionService cachePromotionService;
    private final DatabaseAccessService databaseAccessService;
    private final RedisService redisService;
    private final ArrayBlockingQueue<InventoryTask>  dataUpdatePool;

    private final Cache<String, List<PromotionItemRecord>> localCache;

    @Idempotent(action = "newPromoItem")
    @Transactional
    public String addNewPromotionItem(String productId, int quantity){
        //For fast generate promotion item only.
        String requestId = Utils.getRequestIdFromHeader();
        final String itemId = UUID.randomUUID().toString();
        final PromotedItem promotedItem = PromotedItem.builder()
                .id(itemId)
                .productId(productId)
                .quantity(quantity)
                .build();
        productsRepository.save(promotedItem);

        final PromotedInventory promotedInventory = PromotedInventory.builder()
                .id(UUID.randomUUID().toString())
                .promotedItemId(itemId)
                .quantity(quantity)
                .freezeQuantity(0)
                .build();
        promotedInventoryRepository.save(promotedInventory);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cachePromotionService.refreshStorage(requestId, itemId);
            }
        });
        return itemId;
    }

    public List<PromotionItemRecord> getCurrentPromotedItems(){
        String requestId = Utils.getRequestIdFromHeader();
        final String key = "collection:currentOn";

        return localCache.get(key, k -> {
            Object cachedData = redisService.get(key);
            if(cachedData == null) {
                cachePromotionService.refreshCollectionCurrentPromoted(requestId, key);
                return List.of();
            }
            try {
                CachedMessage<List<PromotionItemRecord>> cache = (CachedMessage<List<PromotionItemRecord>>) cachedData;
                if(cache.getExpireTime().isBefore(LocalDateTime.now())){
                    cachePromotionService.refreshCollectionCurrentPromoted(requestId, key);
                }
                return cache.getData();
            } catch (ClassCastException e) {
                return List.of();
            }
        });
    }
    @Idempotent(action = "lockPromoItem")
    public LockPromotionResult lockPromoItem(String promotedItemId, int quantity) {
        String requestId = Utils.getRequestIdFromHeader();
        String userId = Utils.getUserIdFromHeader();
        String storageKey = "storage:" + promotedItemId;
        try {
            boolean result = cachePromotionService.occupyInventory(storageKey, quantity);
            if(result){
                Supplier<Integer> asyncUpdate = () -> databaseAccessService.freezeInventory(userId, promotedItemId, quantity);
                Consumer<Integer> rollback = affectedRowCount -> {
                    if(affectedRowCount == null || affectedRowCount == 0){
                        try {
                            cachePromotionService.releaseInventory(storageKey, quantity);
                        } catch (RedisInventoryNotExistException e) {
                            cachePromotionService.refreshStorage(requestId, promotedItemId);
                        }
                    }
                };
                final InventoryTask<Integer> integerInventoryTask = InventoryTask.<Integer>builder()
                        .asyncUpdate(asyncUpdate)
                        .rollback(rollback)
                        .build();
                boolean needRollBack = false;
                try {
                    needRollBack = !dataUpdatePool.offer(integerInventoryTask, 10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    needRollBack = true;
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error(e);
                    needRollBack = true;
                }
                if(needRollBack) {
                    rollback.accept(null);
                    return LockPromotionResult.builder().state(LockPromotionResult.State.RETRY_LATER).build();
                }
            }
            return LockPromotionResult.builder().state(result ? LockPromotionResult.State.SUCCESS : LockPromotionResult.State.FAIL_WITH_SOLD_OUT).build();
        } catch (RedisInventoryNotExistException e) {
            cachePromotionService.refreshStorage(requestId, promotedItemId);
            return LockPromotionResult.builder().state(LockPromotionResult.State.RETRY_LATER).build();
        }
    }
}

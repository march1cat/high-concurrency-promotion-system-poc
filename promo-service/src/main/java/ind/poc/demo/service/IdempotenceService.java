package ind.poc.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class IdempotenceService<T> {
    private final RedisService redisService;
    public Object check(String action, String requestId, Supplier<T> businessLogic){
        String key = "idempotence:" + action + ":" + requestId;
        Object doneRecord = redisService.get(key);
        if(doneRecord == null) {
            String lockKey = "lock:" + action + ":" + requestId;
            final boolean isLockGetted = redisService.tryLock(lockKey, requestId, 10L);
            if(isLockGetted){
                try {
                    doneRecord = redisService.get(key);
                    if(doneRecord == null) {
                        T result = businessLogic.get(); // 執行業務邏輯
                        redisService.set(key, result, 300L); // 存入結果，下次直接回傳
                        doneRecord = result;
                    }
                } finally {
                    redisService.releaseLock(lockKey, requestId);
                }
            } else {
                throw new RuntimeException("Duplicated request: " + requestId);
            }
        } else {
            return doneRecord;
        }
        return doneRecord;
    }






}

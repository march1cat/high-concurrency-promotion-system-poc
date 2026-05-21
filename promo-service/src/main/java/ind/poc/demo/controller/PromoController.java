package ind.poc.demo.controller;

import ind.poc.demo.annotation.Idempotent;
import ind.poc.demo.data.*;
import ind.poc.demo.err.RedisInventoryNotExistException;
import ind.poc.demo.service.CachePromotionService;
import ind.poc.demo.service.PromotionService;
import ind.poc.demo.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Log4j2
public class PromoController {

    private final PromotionService promotionService;
    private final ConcurrentHashMap<String, DeferredWaiter> waitingPool;
    @PostMapping("/new")
    public ResponseAddNewPromoItem addNewPromotionProduct(@RequestBody RequestAddNewPromoItem requestAddNewPromoItem){
        try {
            promotionService.addNewPromotionItem(requestAddNewPromoItem.getProductId(), requestAddNewPromoItem.getAvailableQuantity());
            return ResponseAddNewPromoItem.builder()
                    .isSuccess(true)
                    .build();
        } catch (Exception e) {
            return ResponseAddNewPromoItem.builder()
                    .isSuccess(false)
                    .errorDescription(e.getMessage())
                    .build();
        }
    }
    @GetMapping("/currentOn")
    public DeferredResult<List<PromotionItemRecord>> getPromotedItems(){
        String requestId = Utils.getRequestIdFromHeader();
        String userId = Utils.getUserIdFromHeader();
        DeferredResult<List<PromotionItemRecord>> result = new DeferredResult<>(5000L);
        final DeferredWaiter<Void, List<PromotionItemRecord>> waiting = DeferredWaiter.<Void, List<PromotionItemRecord>>builder()
                .waitType(DeferredWaiter.WAIT_FOR.PROMOTED_ITEMS)
                .requestId(requestId)
                .userId(userId)
                .deferredResult(result)
                .build();
        waitingPool.put(requestId, waiting);
        result.onCompletion(() -> {
            waitingPool.remove(requestId);
        });

        final List<PromotionItemRecord> currentPromotedItems = promotionService.getCurrentPromotedItems();
        if(currentPromotedItems != null) {
            result.setResult(currentPromotedItems);
        }
        return result;
    }
    @PostMapping("/occupyPromotion")
    public ResponseMessage occupyPromotion(@RequestBody RequestOccupyPromotion requestOccupyPromotion){
        final LockPromotionResult lockResult = promotionService.lockPromoItem(requestOccupyPromotion.getPromotedItemId(), requestOccupyPromotion.getQuantity());
        if(lockResult.getState() == LockPromotionResult.State.SUCCESS) {
            return ResponseMessage.builder()
                    .isSuccess(true)
                    .build();
        } else {
            return ResponseMessage.builder()
                    .isSuccess(false)
                    .description(lockResult.getState().toString())
                    .build();
        }
    }


}

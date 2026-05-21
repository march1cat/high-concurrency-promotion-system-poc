package ind.poc.demo.webclient;

import ind.poc.demo.webclient.depencies.PromotionItemRecord;
import ind.poc.demo.webclient.depencies.RequestAddNewPromoItem;
import ind.poc.demo.webclient.depencies.ResponseAddNewPromoItem;
import ind.poc.demo.webclient.handler.PromoClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "promo-service", path = "/api/promotions", fallbackFactory = PromoClientFallbackFactory.class)
public interface PromoClient {
    @PostMapping("/new")
    ResponseAddNewPromoItem addNewPromotionProduct(@RequestBody RequestAddNewPromoItem request);
    @GetMapping("currentOn")
    List<PromotionItemRecord> getPromotedItems();
}

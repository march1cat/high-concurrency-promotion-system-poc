package ind.poc.demo.webclient.handler;

import ind.poc.demo.webclient.PromoClient;
import ind.poc.demo.webclient.depencies.PromotionItemRecord;
import ind.poc.demo.webclient.depencies.RequestAddNewPromoItem;
import ind.poc.demo.webclient.depencies.ResponseAddNewPromoItem;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Log4j2
@Component
public class PromoClientFallbackFactory implements FallbackFactory<PromoClient> {
    @Override
    public PromoClient create(Throwable cause) {
        log.error("呼叫 promo-service 發生異常，觸發熔斷降級。錯誤原因: ", cause);
        return new PromoClient() {
            @Override
            public ResponseAddNewPromoItem addNewPromotionProduct(RequestAddNewPromoItem request) {
                return ResponseAddNewPromoItem.builder()
                        .isSuccess(false)
                        .errorDescription("系統繁忙，請稍後再試。原因：" + cause.getMessage())
                        .build();
            }

            @Override
            public List<PromotionItemRecord> getPromotedItems() {
                return Collections.emptyList();
            }

        };
    }
}

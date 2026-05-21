package ind.poc.demo.service;

import ind.poc.demo.data.Product;
import ind.poc.demo.data.PromotedProduct;
import ind.poc.demo.webclient.PromoClient;
import ind.poc.demo.webclient.depencies.PromotionItemRecord;
import ind.poc.demo.webclient.depencies.RequestAddNewPromoItem;
import ind.poc.demo.webclient.depencies.ResponseAddNewPromoItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class PromotoService {

    private final PromoClient promoClient;
    private final DatabaseAccessService databaseAccessService;

    public void addNewItem(Product product, int quantity){
        final ResponseAddNewPromoItem responseAddNewPromoItem = promoClient.addNewPromotionProduct(RequestAddNewPromoItem.builder()
                        .productId(product.getId())
                        .availableQuantity(quantity)
                .build());
        if(!responseAddNewPromoItem.isSuccess()){
            throw new RuntimeException(responseAddNewPromoItem.getErrorDescription());
        }
    }
    @Cacheable(value = "collection", key = "'promoted_products_all'", unless = "#result == null || #result.isEmpty()")
    public List<PromotedProduct> getPromotedProducts() {
        final List<PromotionItemRecord> promotedItems = promoClient.getPromotedItems();
        final List<String> productIds = promotedItems.stream().map(PromotionItemRecord::getProductId).collect(Collectors.toList());
        final List<Product> products = databaseAccessService.getProducts(productIds);
        final Map<String, Product> productsCollection= products.stream().collect(Collectors.toMap(
                Product::getId,
                item -> item
        ));
        final List<PromotedProduct> collect = promotedItems.stream().map(promoItem -> {
            final Product product = productsCollection.get(promoItem.getProductId());
            if (product != null) {
                return PromotedProduct.builder()
                        .name(product.getName())
                        .description(product.getDescription())
                        .id(product.getId())
                        .promotedId(promoItem.getPromotedItemId())
                        .build();
            } else {
                return null;
            }
        }).filter(item -> item != null).collect(Collectors.toList());

        return !promotedItems.isEmpty() ? collect : List.of();
    }
}

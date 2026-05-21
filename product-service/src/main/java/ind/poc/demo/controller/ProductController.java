package ind.poc.demo.controller;

import ind.poc.demo.data.Product;
import ind.poc.demo.data.PromotedProduct;
import ind.poc.demo.data.ResponseMessage;
import ind.poc.demo.service.DatabaseAccessService;
import ind.poc.demo.service.PromotoService;
import ind.poc.demo.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final DatabaseAccessService databaseAccessService;
    private final PromotoService promotoService;

    @GetMapping("/newPromotion")
    public ResponseMessage<Product> randomAddPromo(){
        try {
            Product product = databaseAccessService.randomPickProduct();
            int availableQuantity = (int)(Math.random() * 236456)  % 1000;
            promotoService.addNewItem(product, availableQuantity);
            return ResponseMessage.<Product>builder()
                    .isSuccess(true)
                    .data(product)
                    .build();
        } catch (Exception e) {
            return ResponseMessage.<Product>builder()
                    .isSuccess(false)
                    .description(e.getMessage())
                    .build();
        }
    }

    @GetMapping("/promoted")
    public ResponseMessage<List<PromotedProduct>> getPromotionProducts(){
        try {
            final List<PromotedProduct> results = promotoService.getPromotedProducts();
            return ResponseMessage.<List<PromotedProduct>>builder()
                    .isSuccess(true)
                    .data(results)
                    .build();
        } catch (Exception e) {
            return ResponseMessage.<List<PromotedProduct>>builder()
                    .isSuccess(false)
                    .description(e.getMessage())
                    .build();
        }
    }

}




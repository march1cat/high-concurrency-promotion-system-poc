package ind.poc.demo.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestAddNewPromoItem {
    private String productId;
    private int availableQuantity;
}

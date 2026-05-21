package ind.poc.demo.webclient.depencies;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RequestAddNewPromoItem {
    private String productId;
    private int availableQuantity;
}

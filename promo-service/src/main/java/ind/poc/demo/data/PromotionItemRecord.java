package ind.poc.demo.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionItemRecord {
    private String promotedItemId;
    private String productId;
    private int quantity;
    private boolean isSoldOut;
}

package ind.poc.demo.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestOccupyPromotion {
    private String promotedItemId;
    private int quantity;
}

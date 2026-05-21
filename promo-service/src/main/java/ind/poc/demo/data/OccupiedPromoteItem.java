package ind.poc.demo.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OccupiedPromoteItems") // Replace with your actual table name if different
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupiedPromoteItem {

    @Id
    @Column(name = "Id", length = 36, nullable = false)
    private String id;

    @Column(name = "PromotedItemId", length = 36, nullable = false)
    private String promotedItemId;

    @Column(name = "InventoryId", length = 36, nullable = false)
    private String inventoryId;

    @Column(name = "UserId", length = 36, nullable = false)
    private String userId;

    @Column(name = "Quantity")
    private int quantity;

    @Column(name = "IsCanceled")
    private boolean isCanceled;

}




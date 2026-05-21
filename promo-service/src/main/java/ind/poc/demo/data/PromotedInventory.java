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
@Table(name = "PromotedInventory") // Replace with your actual table name if different
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotedInventory {

    @Id
    @Column(name = "Id", length = 36, nullable = false)
    private String id;

    @Column(name = "PromotedItemId", length = 36, nullable = false)
    private String promotedItemId;

    @Column(name = "Quantity")
    private int quantity;

    @Column(name = "FreezeQuantity")
    private int freezeQuantity;

}




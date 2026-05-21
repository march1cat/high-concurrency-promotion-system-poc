package ind.poc.demo.repository;

import ind.poc.demo.data.PromotedInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotedInventoryRepository extends JpaRepository<PromotedInventory, String> {

    @Query("SELECT p FROM PromotedInventory p where p.promotedItemId=:itemId")
    PromotedInventory getInventory(String itemId);
    @Modifying
    @Query("UPDATE PromotedInventory p SET p.freezeQuantity=p.freezeQuantity+:qty WHERE p.promotedItemId=:itemId and p.quantity - p.freezeQuantity - :qty >= 0")
    int freeze(String itemId, int qty);
}




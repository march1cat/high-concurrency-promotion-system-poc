package ind.poc.demo.service;

import ind.poc.demo.data.OccupiedPromoteItem;
import ind.poc.demo.data.PromotedInventory;
import ind.poc.demo.data.PromotedItem;
import ind.poc.demo.repository.OccupiedPromoteItemRepository;
import ind.poc.demo.repository.PromotedInventoryRepository;
import ind.poc.demo.repository.PromotedProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class DatabaseAccessService {

    private final PromotedInventoryRepository promotedInventoryRepository;
    private final PromotedProductsRepository promotedProductsRepository;
    private final OccupiedPromoteItemRepository occupiedPromoteItemRepository;

    @Transactional(readOnly = true)
    public PromotedInventory getItemInventoryById(String itemId){
        PromotedInventory promotedInventory = promotedInventoryRepository.getInventory(itemId);
        return promotedInventory;
    }

    @Transactional(readOnly = true)
    public List<PromotedItem> getCurrentPromotedItems(){
        return promotedProductsRepository.getCurrentOn();
    }
    @Transactional
    public int freezeInventory(String userId, String promotedItemId, int quantity){
        int affectedRow = promotedInventoryRepository.freeze(promotedItemId, quantity);
        if(affectedRow > 0) {
            PromotedInventory item = getItemInventoryById(promotedItemId);
            final OccupiedPromoteItem occupiedPromoteItem = OccupiedPromoteItem.builder()
                    .id(UUID.randomUUID().toString())
                    .inventoryId(item.getId())
                    .promotedItemId(promotedItemId)
                    .userId(userId)
                    .quantity(quantity)
                    .build();
            occupiedPromoteItemRepository.save(occupiedPromoteItem);
        }
        return affectedRow;
    }
}

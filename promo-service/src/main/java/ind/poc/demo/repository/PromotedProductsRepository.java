package ind.poc.demo.repository;

import ind.poc.demo.data.PromotedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotedProductsRepository extends JpaRepository<PromotedItem, String> {

    @Query("SELECT p FROM PromotedItem p where p.isSoldOut = false")
    List<PromotedItem> getCurrentOn();
}




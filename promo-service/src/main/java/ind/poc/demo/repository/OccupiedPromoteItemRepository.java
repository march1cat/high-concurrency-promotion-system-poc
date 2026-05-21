package ind.poc.demo.repository;

import ind.poc.demo.data.OccupiedPromoteItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OccupiedPromoteItemRepository extends JpaRepository<OccupiedPromoteItem, String> {}




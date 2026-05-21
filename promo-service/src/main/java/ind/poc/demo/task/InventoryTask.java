package ind.poc.demo.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Builder
@AllArgsConstructor
@Getter
public class InventoryTask<T> {
    private final Supplier<T> asyncUpdate;
    private final Consumer<T> rollback;

    public T execute(){
        try {
            T affectedRow = asyncUpdate.get();
            rollback.accept(affectedRow);
            return affectedRow;
        } catch (Exception e) {
            rollback.accept(null);
            throw e;
        }
    }


}

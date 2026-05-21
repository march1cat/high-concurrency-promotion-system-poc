package ind.poc.demo.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LockPromotionResult {
    public enum State {
        SUCCESS,
        FAIL_WITH_SOLD_OUT,
        RETRY_LATER
    }
    private State state;
}

package ind.poc.demo.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.context.request.async.DeferredResult;

@Data
@Builder
@AllArgsConstructor
public class DeferredWaiter<R,T> {
    public enum WAIT_FOR {
        PROMOTED_ITEMS,
        OCCUPY_INVENTORY
    }
    private WAIT_FOR waitType;
    private String requestId;
    private String userId;
    private R request;
    private DeferredResult<T> deferredResult;

    public void response(T data) {
        deferredResult.setResult(data);
    }
}

package ind.poc.demo.webclient.depencies;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseAddNewPromoItem {
    private boolean isSuccess;
    private String errorDescription;
}

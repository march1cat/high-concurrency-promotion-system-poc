package ind.poc.demo.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignHeaderInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        // 1. 獲取當前執行緒（ThreadLocal）中的 Spring Web 請求上下文
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 2. 從當前的外部請求中，取出網關帶過來的 Header
            String requestId = request.getHeader("X-Request-Id");
            String userId = request.getHeader("X-User-Id");

            // 3. 傳遞給 Feign 的下游請求 Header
            if (requestId != null) {
                template.header("X-Request-Id", requestId);
            }
            if (userId != null) {
                template.header("X-User-Id", userId);
            }
        }
    }
}

package ind.poc.demo.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class Utils {
    public static String getRequestIdFromHeader(){
        String requestId = null;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            requestId = request.getHeader("X-Request-Id");
        }
        return requestId;
    }

    public static String getUserIdFromHeader(){
        String requestId = null;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            requestId = request.getHeader("X-User-Id");
        }
        return requestId;
    }
}

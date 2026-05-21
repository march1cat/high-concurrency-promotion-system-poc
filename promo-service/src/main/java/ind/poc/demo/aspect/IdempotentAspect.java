package ind.poc.demo.aspect;

import ind.poc.demo.annotation.Idempotent;
import ind.poc.demo.service.IdempotenceService;
import ind.poc.demo.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Order(-1)
@Log4j2
public class IdempotentAspect {

    private final IdempotenceService idempotenceService;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String requestId = Utils.getRequestIdFromHeader();// 從 Request Header 或參數中取得 requestId
        if(requestId == null) {
            throw new RuntimeException("Request id not found!!");
        }
        return idempotenceService.check(idempotent.action(), requestId, () -> {
            try {
                return joinPoint.proceed(); // 執行被註解的方法 (如 addNewPromotionItem)
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

}

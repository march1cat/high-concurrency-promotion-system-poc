package ind.poc.demo.database;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
@Order(-1)
public class ReadWriteRoutingAspect {

    @Around("@annotation(ind.poc.demo.annotation.DBReadOnly)")
    public Object routeReadReplica(ProceedingJoinPoint pjp) throws Throwable {
        try {
            DataSourceContextHolder.set(DataSourceOperation.READ);
            return pjp.proceed();
        } finally {
            DataSourceContextHolder.clear();
        }
    }

    @Around("@annotation(ind.poc.demo.annotation.UsePrimaryDatabase)")
    public Object routePrimaryDatabase(ProceedingJoinPoint pjp) throws Throwable {
        try {
            DataSourceContextHolder.set(DataSourceOperation.WRITE);
            return pjp.proceed();
        } finally {
            DataSourceContextHolder.clear();
        }
    }

    // Route based on @Transactional(readOnly)
    @Around("@annotation(tx)")
    public Object routeTransactional(ProceedingJoinPoint pjp, Transactional tx) throws Throwable {
        boolean readOnly = tx != null && tx.readOnly();
        try {
            DataSourceContextHolder.set(readOnly ? DataSourceOperation.READ : DataSourceOperation.WRITE);
            return pjp.proceed();
        } finally {
            DataSourceContextHolder.clear();
        }
    }
}




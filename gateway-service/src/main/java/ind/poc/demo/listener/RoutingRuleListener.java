package ind.poc.demo.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class RoutingRuleListener {
    private final ApplicationEventPublisher publisher;

    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefreshScopeRefreshed(RefreshScopeRefreshedEvent event) {
        log.info("偵測到 Nacos 設定檔發生變更，正在重新加載 Gateway 路由規則...");
        // 💡 核心：發送 RefreshRoutesEvent 觸發 Spring Cloud Gateway 重新讀取路由表
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        log.info("Gateway 路由規則動態更新成功！");
    }
}

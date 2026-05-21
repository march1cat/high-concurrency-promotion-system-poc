package ind.poc.demo.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import ind.poc.demo.listener.RedisReceiver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.Executor;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class RedisConfiguration {
    private final NacosConfigManager nacosConfigManager;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用與 Listener 一致的 Jackson 序列化器
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        return template;
    }
    @Bean
    public DefaultRedisScript<Long> redisLockScript() throws NacosException {
        String dataId = "redis_lock_script.lua";
        String group = "DEFAULT_GROUP";
        return generateRedisScript(dataId, group);
    }
    @Bean
    public DefaultRedisScript<Long> redisUnLockScript() throws NacosException {
        String dataId = "redis_unlock_script.lua";
        String group = "DEFAULT_GROUP";
        return generateRedisScript(dataId, group);
    }

    @Bean
    public DefaultRedisScript<Long> redisLockInvScript() throws NacosException {
        String dataId = "inventory_occupy.lua";
        String group = "DEFAULT_GROUP";
        return generateRedisScript(dataId, group);
    }

    @Bean
    public DefaultRedisScript<Long> redisReleaseInvScript() throws NacosException {
        String dataId = "inventory_release.lua";
        String group = "DEFAULT_GROUP";
        return generateRedisScript(dataId, group);
    }

    private DefaultRedisScript<Long> generateRedisScript(String dataId, String group) throws NacosException {
        ConfigService configService = nacosConfigManager.getConfigService();
        String luaContent = configService.getConfig(dataId, group, 5000);
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaContent);
        redisScript.setResultType(Long.class);

        configService.addListener(dataId, group, new Listener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                log.info("Nacos 腳本更新成功! DataID: {}", dataId);
                // 當 Nacos 修改後，Spring 容器中的這個 redisScript 實例會被更新
                redisScript.setScriptText(configInfo);
            }
            @Override
            public Executor getExecutor() {
                return null; // 使用 Nacos 默認線程池
            }
        });
        return redisScript;
    }


    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerCompleteNewOrder
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 訂閱名為 "cache_refresh" 的頻道
        container.addMessageListener(listenerCompleteNewOrder, new PatternTopic("on_complete_refresh_current_promoted"));
        return container;
    }

    @Bean
    MessageListenerAdapter listenerCompleteNewOrder(RedisReceiver receiver) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, "onCompleteRefreshCurrentOn");
        // 關鍵：設定序列化器，否則它會用預設的 JDK 序列化去解析 JSON，導致解析失敗
        adapter.setSerializer(serializer);
        return adapter;
    }


}

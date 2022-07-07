package cn.altaria.currentlimiting.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置
 *
 * @author xuzhou
 * @since 2022/7/7
 */
@Configuration
@ComponentScan(
        basePackages = {"cn.altaria.currentlimiting.config", "cn.altaria.currentlimiting.spring",
                "cn.altaria.currentlimiting.aspect", "cn.altaria.currentlimiting.count.strategy",
                "cn.altaria.currentlimiting.handler", "cn.altaria.currentlimiting.aspect",}
)
public class CurrentLimitingAutoConfiguration {

}

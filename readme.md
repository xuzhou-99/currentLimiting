## 服务限流

### 前言

本模块主要实现对服务请求的限流，通过策略模式加载不同配置类型的限流实现，目前的限流算法是`固定窗口限流算法`,
一定时间内达到限流上限则限制访问。

#### 主要的限流算法

参考文档：

[4种经典限流算法讲解 https://zhuanlan.zhihu.com/p/376564740](https://zhuanlan.zhihu.com/p/376564740)

[架构之高并发：限流 https://www.pdai.tech/md/arch/arch-y-ratelimit.html](https://www.pdai.tech/md/arch/arch-y-ratelimit.html)

* 固定窗口限流算法
* 滑动窗口限流算法
* 漏桶算法
* 令牌桶算法

#### 主要模块

* 限流实现：
    * 注解限流：`@CurrentLimiting`，通过注解添加限流场景、策略、限流上限等参数
    * 动态限流：`LimitFilter`，通过其中的静态方法，动态添加、删除、查看、初始化限流请求
* 限流场景`SceneStrategy`：全局限流、IP限流
* 限流计数器`CountStrategy`：本地缓存、Redis(单机)
* 限流策略`LimitingStrategy`：秒、分、时、天、周、月、年

### 模块依赖

* `spring-boot-configuration-processor`：加载配置类(必要)
* `spring-boot-starter-aop`：aop切面依赖
* `spring-boot-starter-data-redis`：redis依赖

```xml

<dependencies>
    <!-- configuration 加载配置类 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <optional>true</optional>
    </dependency>

    <!--AOP 切面框架-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>

    <!--redis-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
        <version>2.6.6</version>
    </dependency>
</dependencies>
```

### 配置参数

```yml
# ------------------------ 限流配置 ---------------------------- #
security:
  limit:
    # 限流是否开启
    enable: true
    # 限流计数器策略，支持cache(本地)，redis
    count-strategy: redis

spring:
  # ------------------  redis配置 ------------------#
  redis:
    host: 127.0.0.1
    port: 6379
```

### 使用示例

#### 注解限流

通过添加注解`@CurrentLimiting`来标记该请求路由需要进行限流，切面`CurrentLimitingAspect`拦截
统一处理，解析请求路径、请求IP，调用限流过滤器`CurrentLimitingProcessor`根据配置中的`count-strategy`来策略
加载限流计数器，可以选择本地缓存`cache`，或者`redis`来进行限流处理。

* strategy：限流策略
* strategyTime：限流策略组合时长，配合 currentLimiting.strategy().getExTime() 生成过期时间，单位秒
* scene：限流场景
* limit：限流上限

例如，采用了`redis`限流计数器策略，则在`redis`中存放以下记录，每次通过的请求会使得`value`原子增1，
当达到`limit`上限的时候，则抛出`CurrentLimitingException`限流上限异常，组织请求的继续。

* key(拼接的限流key)： limit:ip:/api/limit1:172.31.2.214:60:2
* value(已请求次数计数)： 1

```java
// 使用示例
@Controller
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/limit")
    @ResponseBody
    @CurrentLimiting(strategy = LimitingStrategy.minute_strategy, strategyTime = 1L, scene = SceneStrategy.all, limit = 10L)
    public String limit() {
        System.out.println("测试限流...");
        return null;
    }
}
```

#### 动态限流

对于请求的限流可能并不是实时都要的，可能某一段时间是需要进行限流，所以添加了动态限流的组件，实现核心是`LimitFilter`,
这是一个**全局过滤器**，可以通过调用其中的**静态方法**来实现**动态管理限流**。

* filterCache：动态限流集合，存放所有需要进行限流的请求标识
* doFilter：`Filter`实现方法，对所有记录在`filterCache`中的请求进行限流管理
* method：静态管理方法
    * initFilterCache()：初始化动态限流集合
    * getFilterCache()：获取动态限流集合
    * addLimit(***)：新增限流
    * removeFilterKey(String key)：移除限流
    * removeFilterKey(SceneStrategy scene, String requestTag, String requestIp)：移除限流

```java
// 使用示例
@Slf4j
@Controller
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/limit1")
    @ResponseBody
    public String limit1() {
        log.info("测试 Filter 限流...");
        return null;
    }

    @GetMapping("/add")
    @ResponseBody
    public ApiResponse add(HttpServletRequest request) {
        String ipAddress = RequestUtils.getIpAddress(request);
        log.info("添加 Filter 限流...");
        String limitKey = LimitFilter.addLimit(LimitingStrategy.minute_strategy, SceneStrategy.ip, 1L,
                2, "/api/limit1", ipAddress);
        return ApiResponse.ofSuccess("添加" + limitKey + "限流！");
    }

    @GetMapping("/remove")
    @ResponseBody
    public ApiResponse remove() {
        log.info("移除 Filter 限流...");
        String limitKey = LimitFilter.removeFilterKey(SceneStrategy.all, "/api/limit1", "");
        return ApiResponse.ofSuccess("移除" + limitKey + "限流！");
    }
}

```
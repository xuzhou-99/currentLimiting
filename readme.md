## 服务限流

### 前言

本模块主要实现对服务请求的限流，通过策略模式加载不同配置类型的限流实现，目前的限流算法是`固定窗口限流算法(计数器)`,
一定时间内达到限流上限则限制访问。



#### 主要的限流算法

参考文档：

[4种经典限流算法讲解 https://zhuanlan.zhihu.com/p/376564740](https://zhuanlan.zhihu.com/p/376564740)

[架构之高并发：限流 https://www.pdai.tech/md/arch/arch-y-ratelimit.html](https://www.pdai.tech/md/arch/arch-y-ratelimit.html)

* 固定窗口限流算法
* 滑动窗口限流算法
* 漏桶算法
* 令牌桶算法



### 主要模块

* 限流实现：
    * 注解限流：`@CurrentLimiting`，通过注解添加限流场景、策略、限流上限等参数
    * 动态限流：`LimitFilter`，通过其中的静态方法，动态添加、删除、查看、初始化限流请求
* 限流场景`SceneStrategy`：全局限流、IP限流
* 限流计数器`CountStrategy`：本地缓存、Redis(单机)
* 限流策略`LimitingStrategy`：秒、分、时、天、周、月、年



#### 引用组件

* 目前是发布在github个人仓库，引用时需要添加 github 仓库

```xml

<repositories>
    <!-- github -->
    <repository>
        <id>github</id>
        <!-- https://raw.github.com/用户名/仓库名/分支名 -->
        <url>https://raw.github.com/xuzhou-99/mvn-repo/main</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>

<dependencies>
    
    <!--altaria 限流组件-->
    <dependency>
        <groupId>cn.altaria</groupId>
        <artifactId>currentLimiting</artifactId>
        <version>0.0.2-SNAPSHOT</version>
    </dependency>
</dependencies>
```



#### 配置参数

```yml
# ------------------------ 限流配置 ---------------------------- #
security:
  limit:
    # 限流是否开启
    enable: true
    # 限流计数器策略，支持cache(本地)，redis
    count-strategy: redis

spring:
  # --------------  redis配置，如果想使用redis模式，则配置------------#
  redis:
    host: 127.0.0.1
    port: 6379
```



#### 回调接口实现

提供回调接口 `ILimitingInfoHandle` ，实现其中的 `callback()` 方法，可以获取每次限流/计次信息，自由拓展功能，例如记录数据库日志。

* isSupport：默认需要2个实现类，分别支持 
  * "limit".equals(type)：该实现类支持所有的限流回调
  * "times".equals(type)：该实现类仅支持计次回调
* doBefore：限流或计次前调用方法
  * 计次需要特殊处理，实现该方法，获取appId、secretKey，进行判断是否有效，并处理计次有效时长、总次数
* callback：回调方法，可以实现进行数据库处理等操作

```java
/**
 * @author xuzhou
 * @since 2022/11/14
 */
public interface ILimitingInfoHandle {

    /**
     * 支持限流模式
     *
     * @param type 限流模式：limit限流，times计次
     * @return 支持
     */
    boolean isSupport(String type);

    /**
     * 校验 请求中 AppId和 SecretKey 是否有效
     *
     * @param limitInfo 限流参数信息
     * @return 有效
     */
    default boolean doBefore(LimitingPointInfo limitInfo) {
        return true;
    }


    /**
     * 回调处理
     *
     * @param limitInfo 限流信息
     */
    default void callback(LimitingPointInfo limitInfo) {
        // 计次通过后，剩余次数-1
    }

}
```



### 使用示例

#### 接口限流

##### 注解限流

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
@Slf4j
@Controller
@RequestMapping("/api")
public class TestController {

    @GetMapping("/limit")
    @ResponseBody
    @CurrentLimiting(strategy = LimitingStrategy.MONTH_STRATEGY, strategyTime = 1L, scene = SceneStrategy.ALL, limit = 10L)
    public String limit() {
        System.out.println("测试限流...");
        return null;
    }
}
```



##### 动态限流

对于请求的限流可能并不是实时都要的，可能某一段时间是需要进行限流，所以添加了动态限流的组件，实现核心是`CurrentLimitingFilter`,
这是一个**全局过滤器**，可以通过调用其中的**静态方法**来实现**动态管理限流**。

* filterCache：动态限流集合，存放所有需要进行限流的请求标识
* doFilter：`Filter`实现方法，对所有记录在`filterCache`中的请求进行限流管理
* method：静态管理方法
    * initFilterCache()：初始化动态限流集合
    * getFilterCache()：获取动态限流集合
    * addLimit(***)：新增限流
    * removeFilterKey(String key)：移除限流
    * removeFilterKey(SceneStrategy scene, String requestTag)：移除限流

```java
@Slf4j
@Controller
@RequestMapping("/api")
public class TestController {

    @GetMapping("/limit1")
    @ResponseBody
    public String limit1() {
        log.info("测试 Filter 限流...");
        return null;
    }

    @GetMapping("/add")
    @ResponseBody
    public String add(HttpServletRequest request) {
        log.info("添加 Filter 限流...");
        String limitKey = CurrentLimitingFilter.addLimit(LimitingStrategy.MINUTE_STRATEGY, SceneStrategy.IP, 1L,
                2, "/api/limit1");
        return "添加" + limitKey + "限流！";
    }

    @GetMapping("/remove")
    @ResponseBody
    public String remove(HttpServletRequest request) {

        log.info("移除 Filter 限流...");
        String limitKey = CurrentLimitingFilter.removeFilterKey(SceneStrategy.IP, "/api/limit1");
        return "移除" + limitKey + "限流！";
    }
}
```



#### 接口计次

##### 接口计次

通过添加注解`@CurrentLimiting`来标记该请求路由需要进行计次，切面`CurrentLimitingAspect`拦截
统一处理，解析请求路径、请求IP，调用限流过滤器`TimesLimitingProcessor`根据配置中的`count-strategy`来策略
加载限流计数器，可以选择本地缓存`cache`，或者`redis`来进行限流处理。

* scene：限流场景-**APP**
* strategy、strategyTime、limit：可以通过 **回调接口实现：doBefore/callback**  来从数据库获取并修改，从而实现高度动态自定义

计次请求：

​	支持AppId、ip组合计次

* appId：应用id，放置于请求头
* secretKey：应用密钥，放置于请求头

```java
// 使用示例
@Slf4j
@Controller
@RequestMapping("/api")
public class TestController {

    @GetMapping("/times")
    @ResponseBody
    @CurrentLimiting(strategy = LimitingStrategy.MONTH_STRATEGY, strategyTime = 1L, scene = SceneStrategy.APP, limit = 10L)
    public String times() {
        System.out.println("测试计次...");
        return null;
    }
}
```


### **问题根因分析**

经过仔细分析您的配置和 Spring Boot 的请求处理机制，发现问题出在 `InterceptionConfig` 的拦截路径配置与 `server.servlet.context-path` 的相互作用上。

1.  **Context Path 存在**: 您的 `bootstrap.yml` 配置了 `server.servlet.context-path: /capi`。这意味着应用部署在 `/capi` 根路径下。
2.  **Spring MVC 拦截器机制**: 在 Spring Boot 中，当配置了 `context-path` 时，`InterceptorRegistry.addPathPatterns` 中的路径匹配是**基于 context-path 之后的路径**进行的，而不是包含 context-path 的全路径。
3.  **您的配置**:
    ```java
    registry.addInterceptor(collectionInterceptor)
            .addPathPatterns("/**")  // 意图：匹配所有请求
            .excludePathPatterns("/capi/users/login"); // 问题：这里包含了 context-path
    ```
4.  **实际行为**:
    *   当您请求 `http://localhost:9090/capi/users/logout` 时。
    *   Spring 去掉 context-path `/capi`，剩下的路径是 `/users/logout`。
    *   拦截器配置 `addPathPatterns("/**")` 会匹配 `/users/logout`。
    *   **关键点**: 如果您之前的配置是 `.addPathPatterns("/capi/**")`，这在 Spring MVC 眼里是匹配以 `/capi` 开头的相对路径（即完整 URL 为 `/capi/capi/...`），所以根本匹配不到 `/users/logout`。这就是为什么拦截器之前完全没生效（也没报错）。

### **修正方案**

我们需要调整 `InterceptionConfig.java` 中的路径配置，**去掉 `/capi` 前缀**，因为 Spring MVC 的拦截器配置是相对于 context-path 的。

**目标**:
*   `server.servlet.context-path: /capi` **保持不变**（您要求的）。
*   拦截器路径匹配逻辑修正为相对于 `/capi` 的路径。

**具体修改**:
1.  **修改 `InterceptionConfig.java`**:
    *   `addPathPatterns("/**")`: 保持不变，匹配所有相对路径。
    *   `excludePathPatterns("/users/login")`: **去掉 `/capi` 前缀**。之前的 `/capi/users/login` 是错误的，因为它试图匹配相对路径 `/capi/users/login`（即绝对路径 `/capi/capi/users/login`）。

这样，当请求 `/capi/users/logout` 时：
1.  Context Path `/capi` 被剥离。
2.  相对路径 `/users/logout` 进入拦截器链。
3.  `/**` 匹配成功。
4.  `/users/login` 不匹配（排除逻辑正确）。
5.  进入 `CollectionInterceptor.preHandle`。

### **执行计划**

1.  **修改 `InterceptionConfig.java`**: 将排除路径从 `/capi/users/login` 改为 `/users/login`。

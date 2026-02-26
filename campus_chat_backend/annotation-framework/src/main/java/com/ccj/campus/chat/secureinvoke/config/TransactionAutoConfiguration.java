package com.ccj.campus.chat.secureinvoke.config;


import com.ccj.campus.chat.secureinvoke.annotation.SecureInvokeConfigurer;
import com.ccj.campus.chat.secureinvoke.aspect.SecureInvokeAspect;
import com.ccj.campus.chat.secureinvoke.dao.SecureInvokeRecordDao;
import com.ccj.campus.chat.secureinvoke.mapper.SecureInvokeRecordMapper;
import com.ccj.campus.chat.secureinvoke.service.SecureInvokeService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.function.SingletonSupplier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * @Author ccj
 * @Date 2024-05-15 00:50
 * @Description
 */
@Configuration
@EnableScheduling
@MapperScan(basePackageClasses = SecureInvokeRecordMapper.class)
@Import({SecureInvokeRecordDao.class, SecureInvokeAspect.class})
public class TransactionAutoConfiguration {

    @Nullable
    protected Executor executor;

    @Autowired
    void setConfigurers(ObjectProvider<SecureInvokeConfigurer> configurers) {
        Supplier<SecureInvokeConfigurer> configurer = SingletonSupplier.of(() -> {
            List<SecureInvokeConfigurer> candidates = configurers.stream().toList();
            if (candidates.isEmpty()) {
                return null;
            }
            if (candidates.size() > 1) {
                throw new IllegalStateException("Only one SecureInvokeConfigurer may exist");
            }
            return candidates.get(0);

        });

        executor = Optional.ofNullable(configurer.get()).map(SecureInvokeConfigurer::getSecureInvokeExecutor).orElse(ForkJoinPool.commonPool());
    }

    @Bean
    public SecureInvokeService getSecureInvokeService(SecureInvokeRecordDao dao) {
        return new SecureInvokeService(dao, executor);
    }


}

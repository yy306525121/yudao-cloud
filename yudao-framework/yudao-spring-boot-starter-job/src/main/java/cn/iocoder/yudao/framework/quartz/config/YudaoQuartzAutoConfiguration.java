package cn.iocoder.yudao.framework.quartz.config;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerRegistry;
import cn.iocoder.yudao.framework.quartz.core.scheduler.SchedulerManager;
import cn.iocoder.yudao.framework.quartz.core.service.JobLogFrameworkService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.quartz.JobStoreType;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.Properties;

/**
 * Quartz 自动配置类。
 */
@AutoConfiguration
@EnableConfigurationProperties(QuartzProperties.class)
@Slf4j
public class YudaoQuartzAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "yudao.job", name = "type", havingValue = "quartz")
    @ConditionalOnMissingBean
    public SchedulerFactoryBean schedulerFactoryBean(QuartzProperties properties, ObjectProvider<DataSource> dataSource,
                                                     ObjectProvider<PlatformTransactionManager> transactionManager) {
        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
        factoryBean.setSchedulerName(properties.getSchedulerName());
        factoryBean.setAutoStartup(properties.isAutoStartup());
        factoryBean.setWaitForJobsToCompleteOnShutdown(properties.isWaitForJobsToCompleteOnShutdown());
        factoryBean.setOverwriteExistingJobs(properties.isOverwriteExistingJobs());
        factoryBean.setStartupDelay((int) properties.getStartupDelay().getSeconds());

        Properties quartzProperties = new Properties();
        quartzProperties.putAll(properties.getProperties());
        factoryBean.setQuartzProperties(quartzProperties);

        if (JobStoreType.JDBC.equals(properties.getJobStoreType())) {
            dataSource.ifAvailable(factoryBean::setDataSource);
            transactionManager.ifAvailable(factoryBean::setTransactionManager);
        }
        return factoryBean;
    }

    @Bean
    public SchedulerManager schedulerManager(Optional<Scheduler> scheduler) {
        if (scheduler.isEmpty()) {
            log.info("[定时任务 - Quartz 已禁用][可配置 yudao.job.type=quartz 开启]");
            return new SchedulerManager(null);
        }
        return new SchedulerManager(scheduler.get());
    }

    @Bean
    public JobHandlerRegistry jobHandlerRegistry(ApplicationContext applicationContext) {
        return new JobHandlerRegistry(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public JobLogFrameworkService jobLogFrameworkService() {
        return new JobLogFrameworkService() {
        };
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "yudao.job", name = "type", havingValue = "quartz")
    @EnableScheduling // 开启 Spring 自带的定时任务
    static class SchedulingConfiguration {
    }

}

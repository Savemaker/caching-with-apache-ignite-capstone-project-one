package com.savushkin.ignite;

import org.apache.ignite.cache.spring.SpringCacheManager;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.metric.jmx.JmxMetricExporterSpi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

@SpringBootApplication
@EnableCaching
public class IgniteApplication {

    public static void main(String[] args) {
        SpringApplication.run(IgniteApplication.class, args);
    }


    @Bean
    public SpringCacheManager cacheManager() {
        SpringCacheManager springCacheManager = new SpringCacheManager();
        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName("product");
        cacheConfiguration.setStatisticsEnabled(true);
        cacheConfiguration.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_MINUTE));
        igniteConfiguration.setCacheConfiguration(cacheConfiguration);
        igniteConfiguration.setMetricExporterSpi(new JmxMetricExporterSpi());
        springCacheManager.setConfiguration(igniteConfiguration.setIgniteInstanceName("jcpenney"));
        return springCacheManager;
    }
}

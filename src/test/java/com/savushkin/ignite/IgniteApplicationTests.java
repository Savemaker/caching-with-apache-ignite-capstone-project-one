package com.savushkin.ignite;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.savushkin.ignite.apllication.ProductRepository;
import com.savushkin.ignite.apllication.ProductServiceImpl;
import com.savushkin.ignite.apllication.cache.CacheSizeDTO;
import com.savushkin.ignite.apllication.cache.MetricsDTO;
import com.savushkin.ignite.domain.Product;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.util.List;

@SpringBootTest
@Testcontainers
public class IgniteApplicationTests {

    @Container
    public static final CassandraContainer cassandra
            = (CassandraContainer) new CassandraContainer("cassandra:3.11.2")
            .withCopyFileToContainer(MountableFile.forClasspathResource("jcpenney_com-ecommerce_sample.csv"), "/home/jcpenney_com-ecommerce_sample.csv")
            .withExposedPorts(9042);
    private static final String KEYSPACE_NAME = "task";
    private static final long ONE_MINUTE_ONE_SECOND = 61000L;
    private static final String CACHE_METRICS_URL = "http://localhost:8080/ignite?cmd=cache&cacheName=product&destId=jcpenney";
    private static final String CACHE_SIZE_URL = "http://localhost:8080/ignite?cmd=size&cacheName=product";
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductServiceImpl productService;


    @BeforeAll
    static void setupCassandraConnectionProperties() throws IOException, InterruptedException {
        System.setProperty("spring.data.cassandra.keyspace-name", KEYSPACE_NAME);
        System.setProperty("spring.data.cassandra.contact-points", cassandra.getHost());
        System.setProperty("spring.data.cassandra.port", String.valueOf(cassandra.getMappedPort(9042)));
        createKeyspace(cassandra.getCluster());
        cassandra.execInContainer("sh", "-c", "echo \"CREATE TABLE task.product (uniq_id text PRIMARY KEY,sku text,name_title text,description text,list_price text,sale_price text,category text,category_tree text,average_product_rating text,product_url text,product_image_urls text,brand text,total_number_reviews text,Reviews text);\" | cqlsh");
        cassandra.execInContainer("sh", "-c", "echo \"COPY task.product (uniq_id,sku,name_title,description,list_price,sale_price,category,category_tree,average_product_rating,product_url,product_image_urls,brand,total_number_reviews,Reviews) FROM '/home/jcpenney_com-ecommerce_sample.csv' WITH HEADER=TRUE;\" | cqlsh");
    }

    static void createKeyspace(Cluster cluster) {
        try (Session session = cluster.connect()) {
            session.execute("CREATE KEYSPACE IF NOT EXISTS " + KEYSPACE_NAME + " WITH replication = \n" +
                    "{'class':'SimpleStrategy','replication_factor':'1'};");
        }
    }

    @Test
    void contextLoads() {
    }

    @Test
    void importWorks() {
        List<Product> products = productRepository.findAll();
        Assertions.assertThat(products.size()).isEqualTo(12169);
    }

    @Test
    void igniteCacheWorks() throws InterruptedException {
        long twoSeconds = 2000L;
        long oneSecond = 1000L;
        long start = System.currentTimeMillis();
        productService.getProductById("3928296ab81756dcd77523c5a9852210");
        long end = System.currentTimeMillis();
        Assertions.assertThat(end - start).isGreaterThan(twoSeconds);
        start = System.currentTimeMillis();
        productService.getProductById("3928296ab81756dcd77523c5a9852210");
        end = System.currentTimeMillis();
        Assertions.assertThat(end - start).isLessThan(oneSecond);
    }

    @Test
    void cacheMetricsWithCacheTTLOneMinute() throws InterruptedException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<MetricsDTO> metricsResponse = restTemplate.getForEntity(CACHE_METRICS_URL, MetricsDTO.class);
        ResponseEntity<CacheSizeDTO> cacheSizeResponse = restTemplate.getForEntity(CACHE_SIZE_URL, CacheSizeDTO.class);
        Assertions.assertThat(cacheSizeResponse.getBody().getResponse()).isEqualTo("0");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getMisses()).isEqualTo("0");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getHits()).isEqualTo("0");

        productService.getProductById("3928296ab81756dcd77523c5a9852210");
        metricsResponse = restTemplate.getForEntity(CACHE_METRICS_URL, MetricsDTO.class);
        cacheSizeResponse = restTemplate.getForEntity(CACHE_SIZE_URL, CacheSizeDTO.class);
        Assertions.assertThat(cacheSizeResponse.getBody().getResponse()).isEqualTo("1");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getMisses()).isEqualTo("1");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getHits()).isEqualTo("0");

        productService.getProductById("3928296ab81756dcd77523c5a9852210");
        metricsResponse = restTemplate.getForEntity(CACHE_METRICS_URL, MetricsDTO.class);
        cacheSizeResponse = restTemplate.getForEntity(CACHE_SIZE_URL, CacheSizeDTO.class);
        Assertions.assertThat(cacheSizeResponse.getBody().getResponse()).isEqualTo("1");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getMisses()).isEqualTo("1");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getHits()).isEqualTo("1");

        Thread.sleep(ONE_MINUTE_ONE_SECOND);

        cacheSizeResponse = restTemplate.getForEntity(CACHE_SIZE_URL, CacheSizeDTO.class);
        Assertions.assertThat(cacheSizeResponse.getBody().getResponse()).isEqualTo("0");
        productService.getProductById("3928296ab81756dcd77523c5a9852210");
        metricsResponse = restTemplate.getForEntity(CACHE_METRICS_URL, MetricsDTO.class);
        cacheSizeResponse = restTemplate.getForEntity(CACHE_SIZE_URL, CacheSizeDTO.class);
        Assertions.assertThat(cacheSizeResponse.getBody().getResponse()).isEqualTo("1");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getMisses()).isEqualTo("2");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getHits()).isEqualTo("1");

        productService.getProductById("1550b006f58c6359f1d9b757cbf58436");
        metricsResponse = restTemplate.getForEntity(CACHE_METRICS_URL, MetricsDTO.class);
        cacheSizeResponse = restTemplate.getForEntity(CACHE_SIZE_URL, CacheSizeDTO.class);
        Assertions.assertThat(cacheSizeResponse.getBody().getResponse()).isEqualTo("2");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getMisses()).isEqualTo("3");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getHits()).isEqualTo("1");

        Product product = productService.getProductById("1550b006f58c6359f1d9b757cbf58436");
        metricsResponse = restTemplate.getForEntity(CACHE_METRICS_URL, MetricsDTO.class);
        cacheSizeResponse = restTemplate.getForEntity(CACHE_SIZE_URL, CacheSizeDTO.class);
        Assertions.assertThat(cacheSizeResponse.getBody().getResponse()).isEqualTo("2");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getMisses()).isEqualTo("3");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getHits()).isEqualTo("2");
        Assertions.assertThat(product.getSalePrice()).isEqualTo("31.41");

        productService.updateProductSalePriceById("420.69", "1550b006f58c6359f1d9b757cbf58436");

        Product updatedProduct = productService.getProductById("1550b006f58c6359f1d9b757cbf58436");
        metricsResponse = restTemplate.getForEntity(CACHE_METRICS_URL, MetricsDTO.class);
        cacheSizeResponse = restTemplate.getForEntity(CACHE_SIZE_URL, CacheSizeDTO.class);
        Assertions.assertThat(cacheSizeResponse.getBody().getResponse()).isEqualTo("2");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getMisses()).isEqualTo("3");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getHits()).isEqualTo("3");
        Assertions.assertThat(updatedProduct.getSalePrice()).isEqualTo("420.69");

        Thread.sleep(ONE_MINUTE_ONE_SECOND);

        cacheSizeResponse = restTemplate.getForEntity(CACHE_SIZE_URL, CacheSizeDTO.class);
        Assertions.assertThat(cacheSizeResponse.getBody().getResponse()).isEqualTo("0");
        updatedProduct = productService.getProductById("1550b006f58c6359f1d9b757cbf58436");
        metricsResponse = restTemplate.getForEntity(CACHE_METRICS_URL, MetricsDTO.class);
        cacheSizeResponse = restTemplate.getForEntity(CACHE_SIZE_URL, CacheSizeDTO.class);
        Assertions.assertThat(cacheSizeResponse.getBody().getResponse()).isEqualTo("1");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getMisses()).isEqualTo("4");
        Assertions.assertThat(metricsResponse.getBody().getResponse().getHits()).isEqualTo("3");
        Assertions.assertThat(updatedProduct.getSalePrice()).isEqualTo("420.69");
    }
}

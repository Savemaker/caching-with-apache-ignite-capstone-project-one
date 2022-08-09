package com.savushkin.ignite;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.savushkin.ignite.domain.Product;
import com.savushkin.ignite.integration.ProductRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;

@SpringBootTest
@Testcontainers
class IgniteApplicationTests {

    public static final String KEYSPACE_NAME = "task";
    @Container
    public static final CassandraContainer cassandra
            = (CassandraContainer) new CassandraContainer("cassandra:3.11.2")
            .withCopyFileToContainer(MountableFile.forClasspathResource("jcpenney_com-ecommerce_sample.csv"), "/home/jcpenney_com-ecommerce_sample.csv")
            .withExposedPorts(9042);

    @Autowired
    private ProductRepository productRepository;

    @BeforeAll
    static void setupCassandraConnectionProperties() {
        System.setProperty("spring.data.cassandra.keyspace-name", KEYSPACE_NAME);
        System.setProperty("spring.data.cassandra.contact-points", cassandra.getHost());
        System.setProperty("spring.data.cassandra.port", String.valueOf(cassandra.getMappedPort(9042)));
        createKeyspace(cassandra.getCluster());
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
    void testImportWorks() throws IOException, InterruptedException {
        cassandra.execInContainer("sh", "-c", "echo \"COPY task.product (uniq_id,sku,name_title,description,list_price,sale_price,category,category_tree,average_product_rating,product_url,product_image_urls,brand,total_number_reviews,Reviews) FROM '/home/jcpenney_com-ecommerce_sample.csv' WITH HEADER=TRUE;\" | cqlsh");
        Flux<Product> flux = this.productRepository.findAll();
        StepVerifier.create(flux)
                .expectNextCount(12169)
                .verifyComplete();
    }
}

package com.savushkin.ignite.integration;

import com.savushkin.ignite.domain.Product;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

public interface ProductRepository extends ReactiveCassandraRepository<Product, String> {
}

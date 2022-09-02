package com.savushkin.ignite.apllication;

import com.savushkin.ignite.domain.Product;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface ProductRepository extends CassandraRepository<Product, String> {
}

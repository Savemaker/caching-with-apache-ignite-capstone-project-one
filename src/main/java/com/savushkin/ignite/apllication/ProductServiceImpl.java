package com.savushkin.ignite.apllication;

import com.savushkin.ignite.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Cacheable(value = "product", key = "#id")
    public Product getProductById(String id) {
        try {
            log.info("Performing costly search in db");
            Product product = productRepository.findById(id).orElseThrow(NullPointerException::new);
            Thread.sleep(2000);
            log.info("Got result");
            return product;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @CachePut(cacheNames = "product", key = "#id")
    public Product updateProductSalePriceById(String salePrice, String id) {
        Product product = productRepository.findById(id).orElseThrow(NullPointerException::new);
        product.setSalePrice(salePrice);
        return productRepository.save(product);
    }
}

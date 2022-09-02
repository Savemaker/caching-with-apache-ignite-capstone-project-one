package com.savushkin.ignite.apllication;

import com.savushkin.ignite.domain.Product;

public interface ProductService {
    Product getProductById(String id);

    Product updateProductSalePriceById(String salePrice, String id);
}

package com.savushkin.ignite.api;

import com.savushkin.ignite.apllication.ProductServiceImpl;
import com.savushkin.ignite.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductControllerImpl implements ProductController {

    private final ProductServiceImpl productService;

    @Override
    public ResponseEntity<Product> getProductById(String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Override
    public ResponseEntity<Product> updateProductSalePriceById(String salePrice, String id) {
        return ResponseEntity.ok(productService.updateProductSalePriceById(salePrice, id));
    }
}

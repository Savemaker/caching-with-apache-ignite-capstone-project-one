package com.savushkin.ignite.api;

import com.savushkin.ignite.domain.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/product")
public interface ProductController {
    @GetMapping("/{id}")
    ResponseEntity<Product> getProductById(@PathVariable String id);

    @PostMapping("/{id}")
    ResponseEntity<Product> updateProductSalePriceById(@RequestParam String salePrice, @PathVariable String id);
}

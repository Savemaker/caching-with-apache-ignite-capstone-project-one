package com.savushkin.ignite.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
@Getter
@Setter
@AllArgsConstructor
public class Product {
    @PrimaryKey("uniq_id")
    private String uniqId;
    private String sku;
    @Column("name_title")
    private String nameTitle;
    private String description;
    @Column("list_price")
    private String listPrice;
    @Column("sale_price")
    private String salePrice;
    private String category;
    @Column("category_tree")
    private String categoryTree;
    @Column("average_product_rating")
    private String averageProductRating;
    @Column("product_url")
    private String productUrl;
    @Column("product_image_urls")
    private String productImageUrls;
    private String brand;
    @Column("total_number_reviews")
    private String totalNumberReviews;
    private String reviews;
}

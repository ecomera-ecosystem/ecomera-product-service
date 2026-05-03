package com.ecomera.product.product.mapper;

import com.ecomera.product.shared.common.mapper.BaseMapper;
import com.ecomera.product.shared.common.mapper.BaseMappingConfig;
import com.ecomera.product.product.dto.ProductCreateDto;
import com.ecomera.product.product.dto.ProductDto;
import com.ecomera.product.product.dto.ProductImageCreateDto;
import com.ecomera.product.product.dto.ProductImageDto;
import com.ecomera.product.product.dto.ProductImageUpdateDto;
import com.ecomera.product.product.dto.ProductUpdateDto;
import com.ecomera.product.product.entity.Product;
import com.ecomera.product.product.entity.ProductImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(config = BaseMappingConfig.class)
public interface ProductMapper extends BaseMapper<Product, ProductDto> {

    @Override
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "images", target = "images")
    ProductDto toDto(Product entity);

    ProductImageDto toProductImageDto(ProductImage entity);

    List<ProductImageDto> toProductImageDtoList(List<ProductImage> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    Product toEntity(ProductCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductImage toEntity(ProductImageCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProductUpdateDto dto, @MappingTarget Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "product", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProductImageUpdateDto dto, @MappingTarget ProductImage productImage);
}

package com.ecomera.product.category.mapper;

import com.ecomera.product.category.dto.CategoryCreateDto;
import com.ecomera.product.category.dto.CategoryDto;
import com.ecomera.product.category.dto.CategoryUpdateDto;
import com.ecomera.product.category.entity.Category;
import com.ecomera.product.shared.common.mapper.BaseMapper;
import com.ecomera.product.shared.common.mapper.BaseMappingConfig;
import org.mapstruct.*;

@Mapper(config = BaseMappingConfig.class)
public interface CategoryMapper extends BaseMapper<Category, CategoryDto> {

    @Override
    @Mapping(source = "parent.id", target = "parentId")
    CategoryDto toDto(Category entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    Category toEntity(CategoryCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CategoryUpdateDto dto, @MappingTarget Category category);
}

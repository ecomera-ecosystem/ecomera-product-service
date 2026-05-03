package com.ecomera.product.shared.common.mapper;

import com.ecomera.product.shared.common.audit.BaseEntity;
import org.mapstruct.Mapping;

import java.util.List;

public interface BaseMapper<E extends BaseEntity, D> {

    D toDto(E entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    E toEntity(D d);

    List<D> toDtoList(List<E> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    List<E> toEntityList(List<D> ds);
}

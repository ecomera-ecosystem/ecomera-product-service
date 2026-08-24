package com.ecomera.product.shared.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResilientCacheErrorHandlerTest {

    @Mock
    Cache cache;

    private ResilientCacheErrorHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ResilientCacheErrorHandler();
    }

    @Test
    void shouldSwallowCacheGetError() {
        given(cache.getName()).willReturn("products");

        assertThatCode(() -> handler.handleCacheGetError(new RuntimeException("redis down"), cache, "key-1"))
                .doesNotThrowAnyException();

        verify(cache, times(1)).getName();
    }

    @Test
    void shouldSwallowCachePutError() {
        given(cache.getName()).willReturn("products");

        assertThatCode(() -> handler.handleCachePutError(new RuntimeException("redis down"), cache, "key-1", new Object()))
                .doesNotThrowAnyException();

        verify(cache, times(1)).getName();
    }

    @Test
    void shouldSwallowCacheEvictError() {
        given(cache.getName()).willReturn("products");

        assertThatCode(() -> handler.handleCacheEvictError(new RuntimeException("redis down"), cache, "key-1"))
                .doesNotThrowAnyException();

        verify(cache, times(1)).getName();
    }

    @Test
    void shouldSwallowCacheClearError() {
        given(cache.getName()).willReturn("products");

        assertThatCode(() -> handler.handleCacheClearError(new RuntimeException("redis down"), cache))
                .doesNotThrowAnyException();

        verify(cache, times(1)).getName();
    }

    @Test
    void shouldHandleNullCacheWithoutThrowing() {
        assertThatCode(() -> handler.handleCacheGetError(new RuntimeException("redis down"), null, "key-1"))
                .doesNotThrowAnyException();

        assertThatCode(() -> handler.handleCacheClearError(new RuntimeException("redis down"), null))
                .doesNotThrowAnyException();
    }
}

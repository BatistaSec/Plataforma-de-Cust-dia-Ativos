package com.custody.custody_service.infrastructure.config;

import org.springframework.context.annotation.Configuration;


@Configuration
public class BeanConfiguration {
    // Beans are auto-registered via annotations:
    // - SpringDataPortfolioAssetRepository (@Repository)
    // - SpringDataAssetMovementRepository (@Repository)
    // - AssetMovementRepositoryImpl (@Component)
}

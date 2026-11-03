package com.custody.custody_service.domain.repositories;

import com.custody.custody_service.domain.models.AssetMovement;

public interface AssetMovementRepository {

    AssetMovement save(AssetMovement movement);
}

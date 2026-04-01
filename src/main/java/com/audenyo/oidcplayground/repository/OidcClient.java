package com.audenyo.oidcplayground.repository;

import com.audenyo.oidcplayground.model.entity.StoredRefreshToken;
import java.util.Map;

public interface OidcClient {

    Map<String, Object> refresh(StoredRefreshToken token);
}

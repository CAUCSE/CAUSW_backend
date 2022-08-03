package net.causw.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, String> {
    List<DeviceToken> findByUser_Id(String userId);
}

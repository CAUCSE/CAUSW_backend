package net.causw.app.main.domain.asset.locker.repository.dto;

public record LockerCountByLocation(String locationId, long totalCount, long availableCount) {
}

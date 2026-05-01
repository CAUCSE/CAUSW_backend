package net.causw.app.main.domain.asset.file.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.asset.file.entity.joinEntity.CeremonyAttachImage;

public interface CeremonyAttachImageRepository extends JpaRepository<CeremonyAttachImage, Long> {

	List<CeremonyAttachImage> findAllByCeremony_IdIn(List<String> ceremonyIds);
}

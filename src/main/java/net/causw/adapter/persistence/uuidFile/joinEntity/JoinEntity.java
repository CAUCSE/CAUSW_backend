package net.causw.adapter.persistence.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.application.uuidFile.UuidFileService;


public abstract class JoinEntity extends BaseEntity {

    public abstract UuidFile getUuidFile();

}

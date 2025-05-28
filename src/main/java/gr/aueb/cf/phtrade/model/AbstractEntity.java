package gr.aueb.cf.phtrade.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;


@Getter
@Setter
@MappedSuperclass
public class AbstractEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    //Creation Timestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    //Update Timestamp
    private LocalDateTime updatedAt;

    @Column(unique = true, updatable = false, nullable = false, length = 36)
    private String uuid = UUID.randomUUID().toString();

    @PrePersist
    protected void onCreate() {
        if(createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEntity)) return false;
        AbstractEntity that = (AbstractEntity) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}

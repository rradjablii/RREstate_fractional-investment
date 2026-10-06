package com.rrestate.property;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

/** Document metadata only. File storage is a separate concern (DocumentStorageService, not yet built). */
@Entity
@Table(name = "property_documents")
@Getter
public class PropertyDocument {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;
    @Column(name = "property_id", insertable = false, updatable = false)
    private UUID propertyId;
    private String title;
    private String category;
    private Long sizeBytes;
    private Instant createdAt;

    protected PropertyDocument() {}
    public PropertyDocument(String title, String category, Long sizeBytes, Instant createdAt) {
        this.title = title; this.category = category; this.sizeBytes = sizeBytes; this.createdAt = createdAt;
    }
}

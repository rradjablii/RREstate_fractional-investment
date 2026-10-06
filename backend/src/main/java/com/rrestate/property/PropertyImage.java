package com.rrestate.property;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "property_images")
@Getter
public class PropertyImage {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;
    @Column(name = "property_id", insertable = false, updatable = false)
    private UUID propertyId;
    private String url;
    private String caption;
    private int sortOrder;

    protected PropertyImage() {}
    public PropertyImage(String url, String caption, int sortOrder) { this.url = url; this.caption = caption; this.sortOrder = sortOrder; }
}

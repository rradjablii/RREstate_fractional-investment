package com.rrestate.auth;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "users")
@Getter
public class User {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    @Enumerated(EnumType.STRING) @Setter
    private KycStatus kycStatus = KycStatus.NOT_STARTED;
    private boolean enabled = true;
    private Instant createdAt;
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role") @Enumerated(EnumType.STRING)
    private Set<Role> roles = EnumSet.noneOf(Role.class);

    protected User() {}

    public User(String email, String passwordHash, String firstName, String lastName, Set<Role> roles, Instant now) {
        this.email = email; this.passwordHash = passwordHash; this.firstName = firstName; this.lastName = lastName;
        this.roles = EnumSet.copyOf(roles); this.createdAt = now; this.updatedAt = now;
    }
}

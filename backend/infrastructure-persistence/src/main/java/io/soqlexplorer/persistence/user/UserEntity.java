package io.soqlexplorer.persistence.user;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA representation of an application user.
 *
 * <p>Lives in the persistence module, mapped to/from the pure {@code domain.User} aggregate by
 * {@link UserMapper}. Keeping JPA out of the domain layer is one of the load-bearing rules of
 * this codebase.
 */
@Entity
@Table(name = "app_user")
public class UserEntity {

  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(nullable = false, length = 254)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = false)
  private boolean enabled;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "app_user_role", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role", nullable = false, length = 32)
  @Enumerated(EnumType.STRING)
  private Set<RoleValue> roles = new HashSet<>();

  protected UserEntity() {
    // Required by JPA.
  }

  public UserEntity(
      UUID id,
      String email,
      String passwordHash,
      boolean enabled,
      Instant createdAt,
      Instant updatedAt,
      Set<RoleValue> roles) {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.enabled = enabled;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.roles = new HashSet<>(roles);
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Set<RoleValue> getRoles() {
    return roles;
  }

  public void setRoles(Set<RoleValue> roles) {
    this.roles = roles;
  }
}

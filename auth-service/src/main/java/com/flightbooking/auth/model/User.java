package com.flightbooking.auth.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String username;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String passwordHash;

	@Column(nullable = false)
	private String fullName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role = Role.CUSTOMER;

	@Column(nullable = false)
	private boolean active = true;

	@Column(nullable = false)
	private boolean emailVerified = false;

	@Column(name = "reset_otp")
	private String resetOtp;

	@Column(name = "reset_otp_expiry")
	private Instant resetOtpExpiry;

	@Column(name = "verification_otp")
	private String verificationOtp;

	@Column(name = "verification_otp_expiry")
	private Instant verificationOtpExpiry;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@PrePersist
	protected void onCreate() {
		Instant now = Instant.now();
		if (this.createdAt == null) {
			this.createdAt = now;
		}
		this.updatedAt = now;
		if (this.username == null || this.username.isBlank()) {
			this.username = this.email;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username != null ? username : email;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getFullName() {
		return fullName;
	}

	public Role getRole() {
		return role;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public String getResetOtp() {
		return resetOtp;
	}

	public Instant getResetOtpExpiry() {
		return resetOtpExpiry;
	}

	public String getVerificationOtp() {
		return verificationOtp;
	}

	public Instant getVerificationOtpExpiry() {
		return verificationOtpExpiry;
	}

	public Instant getCreatedAt() {
		return createdAt != null ? createdAt : Instant.now();
	}

	public Instant getUpdatedAt() {
		return updatedAt != null ? updatedAt : Instant.now();
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public void setResetOtp(String resetOtp) {
		this.resetOtp = resetOtp;
	}

	public void setResetOtpExpiry(Instant resetOtpExpiry) {
		this.resetOtpExpiry = resetOtpExpiry;
	}

	public void setVerificationOtp(String verificationOtp) {
		this.verificationOtp = verificationOtp;
	}

	public void setVerificationOtpExpiry(Instant verificationOtpExpiry) {
		this.verificationOtpExpiry = verificationOtpExpiry;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}

package com.freelance.freelancepm.entity;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Top-level enum for invoice lifecycle statuses.
 * <p>
 * Persisted as {@code EnumType.STRING} in the database, so enum constant names
 * must remain stable. Adding new statuses is safe (OCP); removing or renaming
 * existing ones requires a data migration.
 * </p>
 *
 * <h3>Backward-compatibility note</h3>
 * {@code FINAL} and {@code FAILED} exist in production data and are retained
 * even though they are not part of the primary user-facing workflow
 * (DRAFT → SENT → PAID / OVERDUE). Removing them would break existing rows.
 */
public enum InvoiceStatus {

    /* ---- Primary workflow statuses ---- */
    DRAFT("Draft"),
    SENT("Sent"),
    PAID("Paid"),
    OVERDUE("Overdue"),

    /* ---- Legacy / secondary statuses (backward-compatible) ---- */
    FINAL("Finalized"),
    FAILED("Failed"),
    PARTIALLY_PAID("Partially Paid"),
    OVERPAID("Overpaid");

    private final String displayName;

    InvoiceStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns a human-readable label suitable for frontend display.
     * <p>Example: {@code PARTIALLY_PAID → "Partially Paid"}</p>
     */
    public String getDisplayStatus() {
        return displayName;
    }

    /**
     * Serialised to JSON as the enum constant name (e.g. {@code "DRAFT"})
     * so existing API consumers are unaffected.
     */
    @JsonValue
    public String toJson() {
        return name();
    }
}

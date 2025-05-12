package pawa_be.ticket.internal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pawa_be.ticket.external.enumerator.TicketType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_types")
@Data
public class TicketModel {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type_code")
    private TicketType ticketType;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Expiry hours stored as Long for database compatibility
     */
    @Column(name = "expiry_hours", nullable = false)
    private Long expiryHours;

    @Column(name = "expiry_description", nullable = false, length = 500)
    private String expiryDescription;

    @Column(name = "eligibility_requirements", nullable = false, length = 500)
    private String eligibilityRequirements;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Converts the stored hours to a Duration object
     * 
     * @return Duration object representing the expiry interval
     */
    public Duration getExpiryInterval() {
        return Duration.ofHours(expiryHours != null ? expiryHours : 0);
    }

    /**
     * Sets the expiry interval by converting a Duration to hours
     * 
     * @param duration Duration object to convert to hours
     */
    public void setExpiryInterval(Duration duration) {
        this.expiryHours = duration != null ? duration.toHours() : null;
    }
}

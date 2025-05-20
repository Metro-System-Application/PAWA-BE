package pawa_be.payment.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pawa_be.payment.internal.enumeration.TicketStatus;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Setter
@Getter
@Embeddable
public class InvoiceItemKey implements Serializable {

    private UUID invoiceItemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvoiceItemKey that)) return false;
        return Objects.equals(invoiceItemId, that.invoiceItemId) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceItemId, status);
    }

    public void setInvoiceItemId(UUID invoiceItemId) {
        this.invoiceItemId = invoiceItemId;
    }
}

package pawa_be.ticket.internal.model;

import pawa_be.ticket.external.enumerator.TicketType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name="ticket")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TicketModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String typeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType ticketType;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalTime expiryInterval;


}

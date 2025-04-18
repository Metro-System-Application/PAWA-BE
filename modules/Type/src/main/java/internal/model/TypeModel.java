package internal.model;

import internal.Enumerator.TicketType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name="images")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TypeModel {
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

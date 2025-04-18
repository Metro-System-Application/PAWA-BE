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
    private TicketType ticketType;
    private BigDecimal price;
    private LocalTime expiryInterval;


}

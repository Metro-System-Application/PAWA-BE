package pawa_be.ticket.internal.Enumerator;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum TicketType {
    ONE_WAY_4 (
            "One-way ticket (Up to 4 stations)",
            new BigDecimal(8000),
            "Valid for up to 4 stations",
            "Available for everyone"
    ),
    ONE_WAY_8 (
            "One-way ticket (Up to 8 stations)",
            new BigDecimal(12000),
            "Valid for up to 8 stations",
            "Available for everyone"
    ),
    ONE_WAY_X (
            "One-way ticket (More than 8 stations)",
            new BigDecimal(8000),
            "Valid for more than 8 stations",
            "Available for everyone"
    ),
    DAILY (
            "Daily ticket",
            new BigDecimal(40000),
            "Valid for up to 24 hours after activation",
            "Available for everyone"
    ),
    THREE_DAY (
            "Three-day ticket",
            new BigDecimal(90000),
            "Valid for up to 72 hours after activation",
            "Available for everyone"
    ),
    MONTHLY_STUDENT (
            "Monthly student ticket",
            new BigDecimal(150000),
            "Valid for 30 days after activation",
            "Available only if the passenger provided a student ID during registration"
    ),
    MONTHLY_ADULT (
            "Monthly adult ticket",
            new BigDecimal(300000),
            "Valid for 30 days after activation",
            "Available for everyone"
    ),
    FREE (
            "Free ticket",
            new BigDecimal(0),
            "Valid for 30 days after activation",
            "Available for passengers aged 60 and older, or children under 6. Including people with disabilities or revolutionary contributions"
    );

    private final String displayedName;
    private final BigDecimal price;
    private final String expiryDescription;
    private final String eligibilityRequirements;
    TicketType(String displayedName, BigDecimal price, String expiryDescription, String requirementDescription){
        this.displayedName = displayedName;
        this.price = price;
        this.expiryDescription = expiryDescription;
        this.eligibilityRequirements = requirementDescription;
    }


}

package pawa_be.ticket.internal.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pawa_be.ticket.external.enumerator.TicketType;
import pawa_be.ticket.internal.model.TicketModel;
import pawa_be.ticket.internal.repository.TicketTypeRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * Initializes ticket types in the database on application startup
 * Uses the previous hardcoded values from the TicketType enum
 */
@Component
public class TicketTypeInitializer implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(TicketTypeInitializer.class.getName());

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Override
    public void run(String... args) {
        initializeTicketTypes();
    }

    private void initializeTicketTypes() {
        // Skip if data already exists
        if (ticketTypeRepository.count() > 0) {
            logger.info("Ticket types already exist in database. Skipping initialization.");
            return;
        }

        logger.info("Initializing ticket types in database...");

        // ONE_WAY_4
        createTicketType(
                TicketType.ONE_WAY_4,
                "One-way ticket (Up to 4 stations)",
                new BigDecimal(8000),
                Duration.ofHours(24),
                "Valid for up to 4 stations",
                "Available for everyone");

        // ONE_WAY_8
        createTicketType(
                TicketType.ONE_WAY_8,
                "One-way ticket (Up to 8 stations)",
                new BigDecimal(12000),
                Duration.ofHours(24),
                "Valid for up to 8 stations",
                "Available for everyone");

        // ONE_WAY_X
        createTicketType(
                TicketType.ONE_WAY_X,
                "One-way ticket (More than 8 stations)",
                new BigDecimal(8000),
                Duration.ofHours(24),
                "Valid for more than 8 stations",
                "Available for everyone");

        // DAILY
        createTicketType(
                TicketType.DAILY,
                "Daily ticket",
                new BigDecimal(40000),
                Duration.ofHours(24),
                "Valid for up to 24 hours after activation",
                "Available for everyone");

        // THREE_DAY
        createTicketType(
                TicketType.THREE_DAY,
                "Three-day ticket",
                new BigDecimal(90000),
                Duration.ofHours(72),
                "Valid for up to 72 hours after activation",
                "Available for everyone");

        // MONTHLY_STUDENT
        createTicketType(
                TicketType.MONTHLY_STUDENT,
                "Monthly student ticket",
                new BigDecimal(150000),
                Duration.ofDays(30),
                "Valid for 30 days after activation",
                "Available only if the passenger provided a student ID during registration");

        // MONTHLY_ADULT
        createTicketType(
                TicketType.MONTHLY_ADULT,
                "Monthly adult ticket",
                new BigDecimal(300000),
                Duration.ofDays(30),
                "Valid for 30 days after activation",
                "Available for everyone");

        // FREE
        createTicketType(
                TicketType.FREE,
                "Free ticket",
                new BigDecimal(0),
                Duration.ofDays(30),
                "Valid for 30 days after activation",
                "Available for passengers aged 60 and older, or children under 6. Including people with disabilities or revolutionary contributions");

        logger.info("Ticket types initialization complete.");
    }

    private TicketModel createTicketType(
            TicketType ticketType,
            String displayName,
            BigDecimal price,
            Duration expiryInterval,
            String expiryDescription,
            String eligibilityRequirements) {

        TicketModel model = new TicketModel();
        model.setTicketType(ticketType);
        model.setDisplayName(displayName);
        model.setPrice(price);
        model.setExpiryHours(expiryInterval.toHours());
        model.setExpiryDescription(expiryDescription);
        model.setEligibilityRequirements(eligibilityRequirements);
        model.setActive(true);

        return ticketTypeRepository.save(model);
    }
}

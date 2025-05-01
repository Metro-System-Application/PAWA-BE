package pawa_be.ticket.internal.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.profile.internal.repository.PassengerRepository;
import pawa_be.ticket.external.enumerator.TicketType;
import pawa_be.ticket.internal.dto.TypeDto;
import pawa_be.ticket.internal.model.TicketModel;
import pawa_be.ticket.internal.repository.TicketTypeRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketTypeService {

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    /**
     * Initializes default ticket types in the database if they don't exist.
     * This runs when the application starts.
     */
    @PostConstruct
    public void initializeTicketTypes() {
        // Default values to populate database if types don't exist
        if (ticketTypeRepository.count() == 0) {
            createDefaultTicketTypes();
        }
    }

    /**
     * Creates the default ticket types in the database
     */
    private void createDefaultTicketTypes() {
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
    }

    /**
     * Helper method to create a ticket type in the database
     */
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

    /**
     * Get all active ticket types
     */
    public List<TypeDto> getAllTicketTypes() {
        return ticketTypeRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific ticket type by its code
     */
    public TypeDto getTicketTypeByCode(TicketType code) {
        Optional<TicketModel> ticketType = ticketTypeRepository.findById(code);
        return ticketType.map(this::convertToDto).orElse(null);
    }

    /**
     * Get ticket types that a specific passenger is eligible for
     * 
     * @param passengerId The ID of the passenger to check eligibility for
     * @return List of ticket types the passenger is eligible for
     */
    public List<TypeDto> getEligibleTicketTypesForPassenger(String passengerId) {
        // Get passenger information
        PassengerModel passenger = passengerRepository
                .findPassengerModelByPassengerID(passengerId);

        if (passenger == null) {
            return getAllTicketTypes(); // Return all if passenger not found
        }

        // Get all active ticket types
        List<TicketModel> allTicketTypes = ticketTypeRepository.findByActiveTrue();

        // Filter based on eligibility
        return allTicketTypes.stream()
                .filter(ticketType -> isPassengerEligibleForTicket(passenger, ticketType))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Check if a passenger is eligible for a specific ticket type
     */
    private boolean isPassengerEligibleForTicket(PassengerModel passenger, TicketModel ticketType) {
        TicketType type = ticketType.getTicketType();

        // MONTHLY_STUDENT requires student ID
        if (type == TicketType.MONTHLY_STUDENT) {
            return passenger.getStudentID() != null && !passenger.getStudentID().isEmpty();
        }

        // FREE ticket is for seniors, children, disabled, or revolutionary contributors
        if (type == TicketType.FREE) {
            // Check age for senior citizens (60+)
            boolean isSenior = ChronoUnit.YEARS.between(passenger.getPassengerDateOfBirth(), LocalDate.now()) >= 60;

            // Check age for children under 6
            boolean isChild = ChronoUnit.YEARS.between(passenger.getPassengerDateOfBirth(), LocalDate.now()) < 6;

            // Check disability or revolutionary status
            boolean hasSpecialStatus = Boolean.TRUE.equals(passenger.getHasDisability()) ||
                    Boolean.TRUE.equals(passenger.getIsRevolutionary());

            return isSenior || isChild || hasSpecialStatus;
        }

        // All other ticket types are available to everyone
        return true;
    }

    /**
     * Convert ticket type database entity to DTO
     */
    private TypeDto convertToDto(TicketModel model) {
        TypeDto dto = new TypeDto();
        dto.setTicketType(model.getTicketType());
        dto.setTypeName(model.getDisplayName());
        dto.setPrice(model.getPrice());
        dto.setExpiryDescription(model.getExpiryDescription());
        dto.setRequirementDescription(model.getEligibilityRequirements());
        dto.setExpiryInterval(model.getExpiryInterval());
        dto.setActive(model.getActive());
        return dto;
    }

    /**
     * Convert hours stored in database to Duration object
     */
    public Duration getExpiryIntervalAsDuration(TicketModel model) {
        return Duration.ofHours(model.getExpiryHours());
    }

    /**
     * Get ticket types that can be purchased with the given amount
     * 
     * @param price The maximum price the user can spend
     * @return List of ticket types that cost less than or equal to the given price
     */
    public List<TypeDto> getTicketsByPrice(BigDecimal price) {
        if (price == null) {
            return getAllTicketTypes();
        }

        return ticketTypeRepository.findByActiveTrue()
                .stream()
                .filter(ticket -> ticket.getPrice().compareTo(price) <= 0)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}

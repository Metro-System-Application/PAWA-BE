package pawa_be.ticket.internal.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.profile.internal.repository.PassengerRepository;
import pawa_be.ticket.external.enumerator.TicketType;
import pawa_be.ticket.internal.dto.TypeDto;
import pawa_be.ticket.internal.model.TicketModel;
import pawa_be.ticket.internal.repository.TicketTypeRepository;

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
        PassengerModel passenger = passengerRepository
                .findPassengerModelByPassengerID(passengerId);

        if (passenger == null) {
            return getAllTicketTypes();
        }

        List<TicketModel> allTicketTypes = ticketTypeRepository.findByActiveTrue();

        // Filter based on eligibility
        return allTicketTypes.stream()
                .filter(ticketType -> isPassengerEligibleForTicket(passenger, ticketType))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get ticket types that a specific passenger is eligible for using their phone
     * number
     * 
     * @param phone The phone number of the passenger to check eligibility for
     * @return List of ticket types the passenger is eligible for
     */
    public List<TypeDto> getEligibleTicketTypesForPassengerByPhone(String phone) {
        PassengerModel passenger = passengerRepository.findPassengerModelByPassengerPhone(phone);

        if (passenger == null) {
            return getAllTicketTypes();
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
     * Get ticket types that a specific passenger is eligible for using their email
     * 
     * @param email This parameter is kept for backward compatibility but is no
     *              longer used
     * @return List of all available ticket types
     */
    public List<TypeDto> getEligibleTicketTypesForPassengerByEmail(String email) {
        // Since email field has been removed, we just return all ticket types
        return getAllTicketTypes();
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

    /**
     * Get the best ticket type a passenger is eligible for, prioritizing:
     * 1. Free tickets
     * 2. Tickets with longer validity periods
     * 3. Tickets with lower prices
     * 
     * @param passengerId The ID of the passenger to check eligibility for
     * @return The best ticket type option for the passenger, or null if none found
     */
    public TypeDto getBestTicketForPassenger(String passengerId) {
        List<TypeDto> eligibleTickets = getEligibleTicketTypesForPassenger(passengerId);

        if (eligibleTickets.isEmpty()) {
            return null;
        }

        // First check for FREE tickets - highest priority
        List<TypeDto> freeTickets = eligibleTickets.stream()
                .filter(ticket -> ticket.getPrice().compareTo(BigDecimal.ZERO) == 0)
                .collect(Collectors.toList());

        if (!freeTickets.isEmpty()) {
            // If multiple free tickets, get the one with longest validity
            return freeTickets.stream()
                    .max(Comparator.comparing(ticket -> ticket.getExpiryInterval().toHours()))
                    .orElse(freeTickets.get(0));
        }

        // Otherwise, prioritize by value (most hours per unit cost)
        return eligibleTickets.stream()
                .max(Comparator.comparing(ticket -> {
                    // Avoid division by zero
                    if (ticket.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                        return Double.MAX_VALUE; // Free tickets are highest value
                    }
                    // Calculate value ratio: hours per cost unit
                    return ticket.getExpiryInterval().toHours() / ticket.getPrice().doubleValue();
                }))
                .orElse(eligibleTickets.get(0));
    }

    /**
     * Get the best ticket type based on specific passenger attributes
     * rather than requiring a full passenger object.
     * 
     * @param isRevolutionary Whether the passenger has revolutionary status
     * @param hasDisability   Whether the passenger has disability status
     * @param age             The age of the passenger
     * @param studentId       The student ID of the passenger (if a student)
     * @return The best ticket type option based on the attributes, or null if none
     *         found
     */
    public TypeDto getBestTicketByAttributes(
            Boolean isRevolutionary,
            Boolean hasDisability,
            Integer age,
            String studentId) {

        // Get all active ticket types
        List<TicketModel> allTicketTypes = ticketTypeRepository.findByActiveTrue();

        // Check for FREE ticket eligibility (seniors, children, disability,
        // revolutionary)
        boolean eligibleForFree = Boolean.TRUE.equals(isRevolutionary) ||
                Boolean.TRUE.equals(hasDisability) ||
                (age != null && (age >= 60 || age < 6));

        if (eligibleForFree) {
            // Find FREE ticket
            Optional<TicketModel> freeTicket = allTicketTypes.stream()
                    .filter(ticket -> ticket.getTicketType() == TicketType.FREE)
                    .findFirst();

            if (freeTicket.isPresent()) {
                return convertToDto(freeTicket.get());
            }
        }

        // Check for student ticket eligibility
        boolean isStudent = studentId != null && !studentId.trim().isEmpty();
        if (isStudent) {
            // Find MONTHLY_STUDENT ticket
            Optional<TicketModel> studentTicket = allTicketTypes.stream()
                    .filter(ticket -> ticket.getTicketType() == TicketType.MONTHLY_STUDENT)
                    .findFirst();

            if (studentTicket.isPresent()) {
                return convertToDto(studentTicket.get());
            }
        }

        // If no special ticket is applicable, find the best general ticket based on
        // value
        // (which will typically be MONTHLY_ADULT for long-term use)
        return allTicketTypes.stream()
                .filter(ticket -> {
                    TicketType type = ticket.getTicketType();
                    return type != TicketType.FREE &&
                            type != TicketType.MONTHLY_STUDENT;
                })
                .map(this::convertToDto)
                .max(Comparator.comparing(ticket -> {
                    // Calculate value ratio: hours per cost unit
                    if (ticket.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                        return Double.MAX_VALUE; // Free tickets are highest value
                    }
                    return ticket.getExpiryInterval().toHours() / ticket.getPrice().doubleValue();
                }))
                .orElse(null);
    }

    /**
     * Get the best ticket type a passenger is eligible for based on their phone
     * number
     * 
     * @param phone The phone number of the passenger to check eligibility for
     * @return The best ticket type option for the passenger, or null if none found
     */
    public TypeDto getBestTicketForPassengerByPhone(String phone) {
        List<TypeDto> eligibleTickets = getEligibleTicketTypesForPassengerByPhone(phone);

        if (eligibleTickets.isEmpty()) {
            return null;
        }

        // First check for FREE tickets - highest priority
        List<TypeDto> freeTickets = eligibleTickets.stream()
                .filter(ticket -> ticket.getPrice().compareTo(BigDecimal.ZERO) == 0)
                .collect(Collectors.toList());

        if (!freeTickets.isEmpty()) {
            // If multiple free tickets, get the one with longest validity
            return freeTickets.stream()
                    .max(Comparator.comparing(ticket -> ticket.getExpiryInterval().toHours()))
                    .orElse(freeTickets.get(0));
        }

        // Otherwise, prioritize by value (most hours per unit cost)
        return eligibleTickets.stream()
                .max(Comparator.comparing(ticket -> {
                    // Avoid division by zero
                    if (ticket.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                        return Double.MAX_VALUE; // Free tickets are highest value
                    }
                    // Calculate value ratio: hours per cost unit
                    return ticket.getExpiryInterval().toHours() / ticket.getPrice().doubleValue();
                }))
                .orElse(eligibleTickets.get(0));
    }

    /**
     * Get the best ticket type a passenger is eligible for based on their email
     * 
     * @param email The email of the passenger to check eligibility for
     * @return The best ticket type option for the passenger, or null if none found
     */
    public TypeDto getBestTicketForPassengerByEmail(String email) {
        List<TypeDto> eligibleTickets = getEligibleTicketTypesForPassengerByEmail(email);

        if (eligibleTickets.isEmpty()) {
            return null;
        }

        // First check for FREE tickets - highest priority
        List<TypeDto> freeTickets = eligibleTickets.stream()
                .filter(ticket -> ticket.getPrice().compareTo(BigDecimal.ZERO) == 0)
                .collect(Collectors.toList());

        if (!freeTickets.isEmpty()) {
            // If multiple free tickets, get the one with longest validity
            return freeTickets.stream()
                    .max(Comparator.comparing(ticket -> ticket.getExpiryInterval().toHours()))
                    .orElse(freeTickets.get(0));
        }

        // Otherwise, prioritize by value (most hours per unit cost)
        return eligibleTickets.stream()
                .max(Comparator.comparing(ticket -> {
                    // Avoid division by zero
                    if (ticket.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                        return Double.MAX_VALUE; // Free tickets are highest value
                    }
                    // Calculate value ratio: hours per cost unit
                    return ticket.getExpiryInterval().toHours() / ticket.getPrice().doubleValue();
                }))
                .orElse(eligibleTickets.get(0));
    }
}

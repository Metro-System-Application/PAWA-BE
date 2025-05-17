package pawa_be.ticket.internal.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.profile.internal.repository.PassengerRepository;
import pawa_be.ticket.external.enumerator.TicketType;
import pawa_be.ticket.external.model.MetroLineResponse;
import pawa_be.ticket.external.service.MetroLineService;
import pawa_be.ticket.internal.dto.TypeDto;
import pawa_be.ticket.internal.model.TicketModel;
import pawa_be.ticket.internal.repository.TicketTypeRepository;

@Service
public class TicketTypeService {

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private MetroLineService metroLineService;

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
                new BigDecimal(20000),
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

    public TypeDto getBestTicketByMetroLine(String metroLineId) {
        try {
            // Get metro line information
            MetroLineResponse metroLine = metroLineService.getMetroLineById(metroLineId);
            
            if (metroLine != null && metroLine.getMetroLine() != null) {
                // Get the number of stations in the metro line
                List<String> stationOrder = metroLine.getMetroLine().getStationOrder();
                int stationCount = stationOrder != null ? stationOrder.size() : 0;
                
                // Select appropriate ticket type based on station count
                TicketType ticketType;
                if (stationCount <= 4) {
                    ticketType = TicketType.ONE_WAY_4;
                } else if (stationCount <= 8) {
                    ticketType = TicketType.ONE_WAY_8;
                } else {
                    ticketType = TicketType.ONE_WAY_X;
                }
                
                Optional<TicketModel> ticket = ticketTypeRepository.findById(ticketType);
                return ticket.map(this::convertToDto).orElse(null);
            }
            // Metro line not found or invalid
            return null;
        } catch (Exception e) {
            // If there's an error fetching metro line information, return null
            return null;
        }
    }

    public List<TypeDto> getBestTicketsForPassengerWithMetroLine(String email, String metroLineId) {
        // Find passenger directly by email
        PassengerModel passenger = passengerRepository.findPassengerModelByEmail(email);
        
        if (passenger != null) {
            // Check if passenger is eligible for FREE ticket
            boolean isEligibleForFree = Boolean.TRUE.equals(passenger.getIsRevolutionary()) || 
                                       Boolean.TRUE.equals(passenger.getHasDisability()) ||
                                       isBelow6orAbove60(passenger.getPassengerDateOfBirth());
            
            if (isEligibleForFree) {
                // Return only FREE ticket
                List<TypeDto> result = new ArrayList<>();
                Optional<TicketModel> freeTicket = ticketTypeRepository.findById(TicketType.FREE);
                if (freeTicket.isPresent()) {
                    result.add(convertToDto(freeTicket.get()));
                    return result;
                }
            }
            
            // Check if passenger is a student
            boolean isStudent = passenger.getStudentID() != null && !passenger.getStudentID().isEmpty();
            if (isStudent) {
                // Return only MONTHLY_STUDENT ticket
                List<TypeDto> result = new ArrayList<>();
                Optional<TicketModel> studentTicket = ticketTypeRepository.findById(TicketType.MONTHLY_STUDENT);
                if (studentTicket.isPresent()) {
                    result.add(convertToDto(studentTicket.get()));
                    return result;
                }
            }
        }
        
        // If metro line ID is provided and passenger isn't eligible for special tickets,
        // return the best one-way ticket based on the metro line's station count
        if (metroLineId != null && !metroLineId.isEmpty()) {
            List<TypeDto> result = new ArrayList<>();
            TypeDto bestOneWay = getBestTicketByMetroLine(metroLineId);
            if (bestOneWay != null) {
                result.add(bestOneWay);
            }
            return result;
        }
        
        // For passengers not found or not in special categories, return default tickets
        return getDefaultTickets();
    }

    private List<TypeDto> getDefaultTickets() {
        List<TypeDto> defaultTickets = new ArrayList<>();
        
        // Add MONTHLY_ADULT
        Optional<TicketModel> monthlyAdult = ticketTypeRepository.findById(TicketType.MONTHLY_ADULT);
        monthlyAdult.ifPresent(ticket -> defaultTickets.add(convertToDto(ticket)));
        
        // Add ONE_WAY_4 (will be upgraded later with better one-way logic)
        Optional<TicketModel> oneWay4 = ticketTypeRepository.findById(TicketType.ONE_WAY_4);
        oneWay4.ifPresent(ticket -> defaultTickets.add(convertToDto(ticket)));
        
        return defaultTickets;
    }

    private boolean isBelow6orAbove60(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return false;
        }
        
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        return age < 6 || age >= 60;
    }
}

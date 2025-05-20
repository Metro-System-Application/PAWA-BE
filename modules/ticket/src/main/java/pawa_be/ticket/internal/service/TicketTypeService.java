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
import pawa_be.profile.external.dto.ResponsePassengerDTO;
import pawa_be.profile.external.service.IExternalPassengerService;
import pawa_be.ticket.external.enumerator.TicketType;
import pawa_be.ticket.external.model.MetroLineResponse;
import pawa_be.ticket.external.service.MetroLineService;
import pawa_be.ticket.internal.dto.TypeDto;
import pawa_be.ticket.internal.model.TicketModel;
import pawa_be.ticket.internal.repository.TicketTypeRepository;

@Service
class TicketTypeService implements ITicketTypeService {

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private MetroLineService metroLineService;

    @Autowired
    private IExternalPassengerService externalPassengerService;

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
     * Get all active general ticket types, with ONE_WAY type filtered based on metro line
     * 
     * @param metroLineId The ID of the metro line to determine appropriate ONE_WAY ticket
     * @return List of general ticket types including filtered ONE_WAY ticket
     */
    public List<TypeDto> getAllTicketTypes(String metroLineId) {
        List<TypeDto> allTickets = ticketTypeRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // If no metro line provided, return all tickets
        if (metroLineId == null || metroLineId.trim().isEmpty()) {
            return allTickets;
        }
        
        // Filter out all ONE_WAY tickets
        List<TypeDto> filteredTickets = allTickets.stream()
                .filter(ticket -> {
                    TicketType ticketType = ticket.getTicketType();
                    return ticketType != TicketType.ONE_WAY_4 && 
                           ticketType != TicketType.ONE_WAY_8 && 
                           ticketType != TicketType.ONE_WAY_X &&
                           ticketType != TicketType.FREE && 
                           ticketType != TicketType.MONTHLY_STUDENT;
                })
                .collect(Collectors.toList());
        
        // Get the appropriate ONE_WAY ticket for this metro line - it will never be null now
        TypeDto bestOneWay = getBestTicketByMetroLine(metroLineId);
        
        // Create a new list with ONE_WAY ticket at the top
        List<TypeDto> result = new ArrayList<>();
        result.add(bestOneWay); // Add ONE_WAY ticket first
        result.addAll(filteredTickets); // Add all other tickets
        
        return result;
    }
    
    /**
     * Get all guest ticket types (ONE_WAY based on metro line, DAILY, THREE_DAY, MONTHLY_ADULT)
     * 
     * @param metroLineId The ID of the metro line to determine appropriate ONE_WAY ticket
     * @return List of guest-appropriate ticket types
     */
    public List<TypeDto> getGuestTicketTypes(String metroLineId) {
        // Get all tickets
        List<TypeDto> allTickets = ticketTypeRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // Keep only ticket types that should be available to guests
        List<TypeDto> guestTickets = allTickets.stream()
                .filter(ticket -> {
                    TicketType ticketType = ticket.getTicketType();
                    return ticketType == TicketType.DAILY || 
                           ticketType == TicketType.THREE_DAY || 
                           ticketType == TicketType.MONTHLY_ADULT;
                })
                .collect(Collectors.toList());
        
        // Add the appropriate ONE_WAY ticket based on metro line at the top
        if (metroLineId != null && !metroLineId.trim().isEmpty()) {
            TypeDto bestOneWay = getBestTicketByMetroLine(metroLineId);
            
            // Create a new list with ONE_WAY ticket at the top
            List<TypeDto> result = new ArrayList<>();
            result.add(bestOneWay); // Add ONE_WAY ticket first
            result.addAll(guestTickets); // Add all other tickets
            return result;
        }
        
        return guestTickets;
    }
    
    /**
     * Get student ticket types (same as guest tickets but with MONTHLY_STUDENT instead of MONTHLY_ADULT)
     * 
     * @param metroLineId The ID of the metro line to determine appropriate ONE_WAY ticket
     * @return List of student-appropriate ticket types
     */
    public List<TypeDto> getStudentTicketTypes(String metroLineId) {
        // Get all tickets
        List<TypeDto> allTickets = ticketTypeRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // Keep only ticket types that should be available to students
        List<TypeDto> studentTickets = allTickets.stream()
                .filter(ticket -> {
                    TicketType ticketType = ticket.getTicketType();
                    return ticketType == TicketType.DAILY || 
                           ticketType == TicketType.THREE_DAY || 
                           ticketType == TicketType.MONTHLY_STUDENT;
                })
                .collect(Collectors.toList());
        
        // Add the appropriate ONE_WAY ticket based on metro line at the top
        if (metroLineId != null && !metroLineId.trim().isEmpty()) {
            TypeDto bestOneWay = getBestTicketByMetroLine(metroLineId);
            
            // Create a new list with ONE_WAY ticket at the top
            List<TypeDto> result = new ArrayList<>();
            result.add(bestOneWay); // Add ONE_WAY ticket first
            result.addAll(studentTickets); // Add all other tickets
            return result;
        }
        
        return studentTickets;
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
                if (ticket.isPresent()) {
                    return convertToDto(ticket.get());
                }
            }
            
            // Default to ONE_WAY_4 if metro line not found, invalid, or ticket not found
            Optional<TicketModel> defaultTicket = ticketTypeRepository.findById(TicketType.ONE_WAY_4);
            return defaultTicket.map(this::convertToDto).orElseThrow(() -> 
                new RuntimeException("ONE_WAY_4 ticket type not found in database"));
            
        } catch (Exception e) {
            // If there's any error, default to ONE_WAY_4
            Optional<TicketModel> defaultTicket = ticketTypeRepository.findById(TicketType.ONE_WAY_4);
            return defaultTicket.map(this::convertToDto).orElseThrow(() -> 
                new RuntimeException("ONE_WAY_4 ticket type not found in database"));
        }
    }

    /**
     * Get the best tickets for a passenger with metro line
     * @param email The email of the passenger
     * @param metroLineId The metro line ID (required)
     * @return List of best ticket types for the passenger
     */
    public List<TypeDto> getBestTicketsForPassengerWithMetroLine(String email, String metroLineId) {
        // Validate input
        if (metroLineId == null || metroLineId.isEmpty()) {
            return new ArrayList<>(); // Return empty list if metro line ID is not provided
        }
        
        // Find passenger directly by email
        Optional<ResponsePassengerDTO> passenger = externalPassengerService.getPassengerByEmail(email);
        
        if (passenger.isPresent()) {
            // A. Eligible users (revolutionary, disabled, or age below 6 or above 60) get FREE ticket
            boolean isEligibleForFree = Boolean.TRUE.equals(passenger.get().getIsRevolutionary()) ||
                                       Boolean.TRUE.equals(passenger.get().getHasDisability()) ||
                                       isBelow6orAbove60(passenger.get().getPassengerDateOfBirth());
            
            if (isEligibleForFree) {
                // Return only FREE ticket
                List<TypeDto> result = new ArrayList<>();
                Optional<TicketModel> freeTicket = ticketTypeRepository.findById(TicketType.FREE);
                if (freeTicket.isPresent()) {
                    result.add(convertToDto(freeTicket.get()));
                    return result;
                }
            }
            
            // B. Student users get student ticket types
            boolean isStudent = passenger.get().getStudentID() != null && !passenger.get().getStudentID().isEmpty();
            if (isStudent) {
                return getStudentTicketTypes(metroLineId);
            }
            
            // C. Normal users get all standard ticket types
            return getAllTicketTypes(metroLineId);
        }
        
        // If no passenger found, return guest ticket types
        return getGuestTicketTypes(metroLineId);
    }

    private boolean isBelow6orAbove60(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return false;
        }
        
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        return age < 6 || age >= 60;
    }
}

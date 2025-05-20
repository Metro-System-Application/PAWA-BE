package pawa_be.payment.internal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.infrastructure.common.validation.exceptions.NotFoundException;
import pawa_be.payment.internal.dto.*;
import pawa_be.payment.internal.enumeration.InvoiceItemSortField;
import pawa_be.payment.internal.enumeration.TicketStatus;
import pawa_be.payment.internal.model.InvoiceItemModel;
import pawa_be.payment.internal.model.InvoiceModel;
import pawa_be.payment.internal.repository.InvoiceItemRepository;
import pawa_be.payment.internal.repository.InvoiceRepository;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.profile.internal.repository.PassengerRepository;
import pawa_be.ticket.external.enumerator.TicketType;
import pawa_be.ticket.external.service.IExternalTicketService;
import pawa_be.ticket.internal.model.TicketModel;
import pawa_be.ticket.internal.repository.TicketTypeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
class InvoiceService implements IInvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private IExternalTicketService externalTicketService;

    /**
     * Create a new invoice with its items after payment is successful
     *
     * @param requestCreateInvoiceDTO DTO containing invoice creation data
     * @return Response with created invoice ID
     */
    @Transactional
    public ResponseCreateInvoiceDTO createInvoice(RequestCreateInvoiceDTO requestCreateInvoiceDTO, String transactionId) {
        // Get passenger by ID
        PassengerModel passengerModel = passengerRepository
                .findPassengerModelByPassengerID(requestCreateInvoiceDTO.getPassengerId());

        // Calculate total price from cart items
        BigDecimal totalPrice = requestCreateInvoiceDTO.getCartItems().stream()
                .map(ticket -> {
                    BigDecimal curPrice = ticket.getPrice();
                    return curPrice.multiply(BigDecimal.valueOf(ticket.getAmount()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create invoice (assuming payment is already successful)
        InvoiceModel invoiceModel = new InvoiceModel();
        invoiceModel.setPassengerModel(passengerModel);
        invoiceModel.setEmail(requestCreateInvoiceDTO.getEmail());
        if (transactionId != null) {
            invoiceModel.setStripeId(transactionId);
        }
        invoiceModel.setTotalPrice(totalPrice);
        // Both createdAt and purchasedAt will be set by @CreationTimestamp

        // Save invoice
        InvoiceModel savedInvoice = invoiceRepository.save(invoiceModel);

        // Create and save invoice items
        List<InvoiceItemModel> invoiceItems = requestCreateInvoiceDTO.getCartItems().stream()
                .flatMap(cartItem -> {
                    int amount = (int) cartItem.getAmount();
                    BigDecimal unitPrice = transactionId != null
                            ? cartItem.getPrice().divide(BigDecimal.valueOf(amount))
                            : cartItem.getPrice();
                    return IntStream.range(0, amount).mapToObj(i -> {
                        InvoiceItemModel item = new InvoiceItemModel();
                        item.setInvoiceModel(savedInvoice);
                        item.setTicketType(cartItem.getTicketType());
                        item.setPrice(unitPrice);
                        item.setLineId(cartItem.getLineId());
                        item.setLineName(cartItem.getLineName());
                        item.setStartStation(cartItem.getStartStation());
                        item.setEndStation(cartItem.getEndStation());
                        item.setDuration(externalTicketService.getTicketDuration(cartItem.getTicketType()));
                        if (item.getTicketType().equals("ONE_WAY_4")
                                || item.getTicketType().equals("ONE_WAY_8")
                                || item.getTicketType().equals("ONE_WAY_X")) {
                            item.setStatus(TicketStatus.ACTIVE);
                            item.setActivatedAt(LocalDateTime.now());
                        } else {
                            item.setStatus(TicketStatus.INACTIVE);
                        }
                        return item;
                    });
                })
                .collect(Collectors.toList());

        invoiceItemRepository.saveAll(invoiceItems);

        return new ResponseCreateInvoiceDTO(
                savedInvoice.getInvoiceId(),
                "Invoice created successfully");
    }

    /**
     * Get invoice by ID
     *
     * @param invoiceId ID of the invoice
     * @return Invoice DTO with items
     */
    public InvoiceDTO getInvoiceById(UUID invoiceId) {
        InvoiceModel invoiceModel = invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new NotFoundException(String.format("Invoice with ID '%s' not found", invoiceId)));

        return convertToInvoiceDTO(invoiceModel);
    }

    /**
     * Get all invoices for a passenger
     *
     * @param passengerId ID of the passenger
     * @return List of invoice DTOs
     */
    public List<InvoiceDTO> getInvoicesByPassengerId(String passengerId) {
        List<InvoiceModel> invoices = invoiceRepository.findByPassengerModel_PassengerID(passengerId);

        return invoices.stream()
                .map(this::convertToInvoiceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all invoices for a passenger by email
     *
     * @param email Email of the passenger
     * @return List of invoice DTOs
     */
    public List<InvoiceDTO> getInvoicesByEmail(String email) {
        List<InvoiceModel> invoices = invoiceRepository.findByEmail(email);

        return invoices.stream()
                .map(this::convertToInvoiceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Activates a ticket by its invoice item ID
     * Sets the activatedAt to current time and calculates expiredAt based on ticket type
     * 
     * @param invoiceItemId ID of the invoice item
     * @return GenericResponseDTO with success status and message
     */
    @Transactional
    public GenericResponseDTO<InvoiceItemDTO> activateTicket(UUID invoiceItemId) {
        // Find invoice item
        InvoiceItemModel invoiceItem = invoiceItemRepository.findById(invoiceItemId)
                .orElseThrow(() -> new NotFoundException(String.format("Invoice item with ID '%s' not found", invoiceItemId)));

        LocalDateTime now = LocalDateTime.now();
        
        // First check if ticket has expired
        if (invoiceItem.getExpiredAt() != null && now.isAfter(invoiceItem.getExpiredAt())) {
            invoiceItem.setStatus(TicketStatus.EXPIRED);
            invoiceItemRepository.save(invoiceItem);
            return new GenericResponseDTO<>(false, "Ticket has expired", convertToInvoiceItemDTO(invoiceItem));
        }

        // Then check if ticket is already activated but not expired
        if (invoiceItem.getActivatedAt() != null) {
            updateTicketStatus(invoiceItem);
            invoiceItemRepository.save(invoiceItem);
            return new GenericResponseDTO<>(false, "Ticket has been activated", convertToInvoiceItemDTO(invoiceItem));
        }

        // Get ticket type to determine duration
        TicketModel ticketType = ticketTypeRepository.findById(TicketType.valueOf(invoiceItem.getTicketType()))
                .orElseThrow(() -> new NotFoundException(String.format("Ticket type '%s' not found", invoiceItem.getTicketType())));

        // Set activation and expiration times
        invoiceItem.setActivatedAt(now);
        
        // Calculate expiration time based on ticket type's expiry interval
        LocalDateTime expirationTime = now.plus(ticketType.getExpiryInterval());
        invoiceItem.setExpiredAt(expirationTime);
        
        // Update status to ACTIVE
        invoiceItem.setStatus(TicketStatus.ACTIVE);
        
        // Save the updated invoice item
        invoiceItemRepository.save(invoiceItem);
        
        return new GenericResponseDTO<>(true, "Ticket activated successfully", convertToInvoiceItemDTO(invoiceItem));
    }

    private void updateTicketStatus(InvoiceItemModel invoiceItem) {
        LocalDateTime now = LocalDateTime.now();
        
        if (invoiceItem.getActivatedAt() == null) {
            invoiceItem.setStatus(TicketStatus.INACTIVE);
        } else if (invoiceItem.getExpiredAt() != null && now.isAfter(invoiceItem.getExpiredAt())) {
            invoiceItem.setStatus(TicketStatus.EXPIRED);
        } else {
            invoiceItem.setStatus(TicketStatus.ACTIVE);
        }
    }

    private InvoiceDTO convertToInvoiceDTO(InvoiceModel invoiceModel) {
        List<InvoiceItemModel> items = invoiceItemRepository.findByInvoiceModel(invoiceModel);
        
        // Update status of each item before converting to DTO
        items.forEach(this::updateTicketStatus);
        invoiceItemRepository.saveAll(items);

        List<InvoiceItemDTO> itemDTOs = items.stream()
                .map(item -> new InvoiceItemDTO(
                        item.getInvoiceItemId(),
                        item.getTicketType(),
                        item.getStatus(),
                        item.getPrice(),
                        item.getActivatedAt(),
                        item.getExpiredAt(),
                        item.getLineId(),
                        item.getLineName(),
                        item.getStartStation(),
                        item.getEndStation(),
                        item.getDuration()))
                .collect(Collectors.toList());

        return new InvoiceDTO(
                invoiceModel.getInvoiceId(),
                invoiceModel.getPassengerModel().getPassengerID(),
                invoiceModel.getEmail(),
                invoiceModel.getTotalPrice(),
                invoiceModel.getPurchasedAt(),
                itemDTOs);
    }

    public List<InvoiceItemDTO> getMyInvoiceItemsByStatus(String passengerId, TicketStatus status) {
        List<InvoiceModel> invoices = invoiceRepository.findByPassengerModel_PassengerID(passengerId);
        
        List<InvoiceItemModel> allItems = new ArrayList<>();
        for (InvoiceModel invoice : invoices) {
            List<InvoiceItemModel> items = invoiceItemRepository.findByInvoiceModel(invoice);
            allItems.addAll(items);
        }
        
        // Update status of all items before filtering
        allItems.forEach(this::updateTicketStatus);
        invoiceItemRepository.saveAll(allItems);
        
        // Filter by requested status
        return allItems.stream()
                .filter(item -> item.getStatus() == status)
                .map(item -> new InvoiceItemDTO(
                        item.getInvoiceItemId(),
                        item.getTicketType(),
                        item.getStatus(),
                        item.getPrice(),
                        item.getActivatedAt(),
                        item.getExpiredAt(),
                        item.getLineId(),
                        item.getLineName(),
                        item.getStartStation(),
                        item.getEndStation(),
                        item.getDuration()))
                .collect(Collectors.toList());
    }

    public Page<InvoiceItemDTO> getInvoiceItemsPaginated(
            String passengerId,
            int page,
            int size,
            pawa_be.payment.internal.enumeration.InvoiceItemSortField sortBy,
            String sortDirection) {
        
        // Create pageable object with sorting
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy.getFieldName()));
        
        // Get paginated items
        Page<InvoiceItemModel> invoiceItemsPage = invoiceItemRepository.findAllByPassengerId(passengerId, pageable);
        
        // Update status of all items before returning
        invoiceItemsPage.getContent().forEach(this::updateTicketStatus);
        invoiceItemRepository.saveAll(invoiceItemsPage.getContent());
        
        // Map to DTOs
        return invoiceItemsPage.map(item -> new InvoiceItemDTO(
                item.getInvoiceItemId(),
                item.getTicketType(),
                item.getStatus(),
                item.getPrice(),
                item.getActivatedAt(),
                item.getExpiredAt(),
                item.getLineId(),
                item.getLineName(),
                item.getStartStation(),
                item.getEndStation(),
                item.getDuration()));
    }

    public Page<InvoiceItemDTO> getInvoiceItemsByStatusPaginated(
            String passengerId,
            TicketStatus status,
            int page,
            int size,
            InvoiceItemSortField sortBy,
            String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy.getFieldName()));

        Page<InvoiceItemModel> filteredPage = invoiceItemRepository
                .findAllByInvoiceModel_PassengerModel_PassengerIDAndStatus(passengerId, status, pageable);

        filteredPage.getContent().forEach(this::updateTicketStatus);
        invoiceItemRepository.saveAll(filteredPage.getContent());

        return filteredPage.map(this::convertToInvoiceItemDTO);
    }

    private InvoiceItemDTO convertToInvoiceItemDTO(InvoiceItemModel item) {
        return new InvoiceItemDTO(
            item.getInvoiceItemId(),
            item.getTicketType(),
            item.getStatus(),
            item.getPrice(),
            item.getActivatedAt(),
            item.getExpiredAt(),
            item.getLineId(),
            item.getLineName(),
            item.getStartStation(),
            item.getEndStation(),
            item.getDuration()
        );
    }
}

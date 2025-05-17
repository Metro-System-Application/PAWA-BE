package pawa_be.payment.internal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.infrastructure.common.validation.exceptions.NotFoundException;
import pawa_be.payment.internal.dto.*;
import pawa_be.payment.internal.enumeration.TicketStatus;
import pawa_be.payment.internal.model.InvoiceItemModel;
import pawa_be.payment.internal.model.InvoiceModel;
import pawa_be.payment.internal.repository.InvoiceItemRepository;
import pawa_be.payment.internal.repository.InvoiceRepository;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.profile.internal.repository.PassengerRepository;
import pawa_be.ticket.external.enumerator.TicketType;
import pawa_be.ticket.internal.model.TicketModel;
import pawa_be.ticket.internal.repository.TicketTypeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
                .map(CartItemForInvoiceDTO::getPrice)
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
                .map(cartItem -> {
                    InvoiceItemModel item = new InvoiceItemModel();
                    item.setInvoiceModel(savedInvoice);
                    item.setTicketType(cartItem.getTicketType());
                    item.setStatus(TicketStatus.INACTIVE);
                    item.setPrice(cartItem.getPrice());
                    item.setLineID(cartItem.getLineID());
                    item.setLineName(cartItem.getLineName());
                    item.setStartStation(cartItem.getStartStation());
                    item.setEndStation(cartItem.getEndStation());
                    item.setDuration(cartItem.getAmount() > 0 ? (int)cartItem.getAmount() : 1);
                    // activatedAt and expiredAt will be set when the ticket is activated
                    return item;
                })
                .collect(Collectors.toList());

        invoiceItemRepository.saveAll(invoiceItems);

        return new ResponseCreateInvoiceDTO(
                savedInvoice.getInvoiceID(),
                "Invoice created successfully");
    }

    /**
     * Get invoice by ID
     *
     * @param invoiceId ID of the invoice
     * @return Invoice DTO with items
     */
    public InvoiceDTO getInvoiceById(UUID invoiceId) {
        InvoiceModel invoiceModel = invoiceRepository.findByInvoiceID(invoiceId)
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
    public GenericResponseDTO activateTicket(UUID invoiceItemId) {
        // Find invoice item
        InvoiceItemModel invoiceItem = invoiceItemRepository.findById(invoiceItemId)
                .orElseThrow(() -> new NotFoundException(String.format("Invoice item with ID '%s' not found", invoiceItemId)));

        LocalDateTime now = LocalDateTime.now();
        
        // First check if ticket has expired
        if (invoiceItem.getExpiredAt() != null && now.isAfter(invoiceItem.getExpiredAt())) {
            invoiceItem.setStatus(TicketStatus.EXPIRED);
            invoiceItemRepository.save(invoiceItem);
            return new GenericResponseDTO<>(false, "Ticket has expired", null);
        }

        // Then check if ticket is already activated but not expired
        if (invoiceItem.getActivatedAt() != null) {
            updateTicketStatus(invoiceItem);
            invoiceItemRepository.save(invoiceItem);
            return new GenericResponseDTO<>(false, "Ticket has been activated", null);
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
        
        return new GenericResponseDTO<>(true, "Ticket activated successfully", null);
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
                        item.getInvoiceItemID(),
                        item.getTicketType(),
                        item.getStatus(),
                        item.getPrice(),
                        item.getActivatedAt(),
                        item.getExpiredAt(),
                        item.getLineID(),
                        item.getLineName(),
                        item.getStartStation(),
                        item.getEndStation(),
                        item.getDuration()))
                .collect(Collectors.toList());

        return new InvoiceDTO(
                invoiceModel.getInvoiceID(),
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
                        item.getInvoiceItemID(),
                        item.getTicketType(),
                        item.getStatus(),
                        item.getPrice(),
                        item.getActivatedAt(),
                        item.getExpiredAt(),
                        item.getLineID(),
                        item.getLineName(),
                        item.getStartStation(),
                        item.getEndStation(),
                        item.getDuration()))
                .collect(Collectors.toList());
    }
}

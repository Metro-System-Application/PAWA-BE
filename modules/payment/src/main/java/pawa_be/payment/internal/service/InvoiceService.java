package pawa_be.payment.internal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawa_be.infrastructure.common.validation.exceptions.NotFoundException;
import pawa_be.payment.internal.dto.*;
import pawa_be.payment.internal.model.InvoiceItemModel;
import pawa_be.payment.internal.model.InvoiceModel;
import pawa_be.payment.internal.repository.InvoiceItemRepository;
import pawa_be.payment.internal.repository.InvoiceRepository;
import pawa_be.profile.internal.model.PassengerModel;
import pawa_be.profile.internal.repository.PassengerRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    /**
     * Create a new invoice with its items after payment is successful
     *
     * @param requestCreateInvoiceDTO DTO containing invoice creation data
     * @return Response with created invoice ID
     */
    @Transactional
    public ResponseCreateInvoiceDTO createInvoice(RequestCreateInvoiceDTO requestCreateInvoiceDTO) {
        // Get passenger by ID
        PassengerModel passengerModel = passengerRepository
                .findPassengerModelByPassengerID(requestCreateInvoiceDTO.getPassengerId());

        if (passengerModel == null) {
            throw new NotFoundException(String.format("Passenger with ID '%s' not found",
                    requestCreateInvoiceDTO.getPassengerId()));
        }

        // Calculate total price from cart items
        BigDecimal totalPrice = requestCreateInvoiceDTO.getCartItems().stream()
                .map(CartItemForInvoiceDTO::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create invoice (assuming payment is already successful)
        InvoiceModel invoiceModel = new InvoiceModel();
        invoiceModel.setPassengerModel(passengerModel);
        invoiceModel.setEmail(requestCreateInvoiceDTO.getEmail());
        invoiceModel.setTotalPrice(totalPrice);
        // Both createdAt and purchasedAt will be set by @CreationTimestamp

        // Save invoice
        InvoiceModel savedInvoice = invoiceRepository.save(invoiceModel);

        // Create and save invoice items
        List<InvoiceItemModel> invoiceItems = requestCreateInvoiceDTO.getCartItems().stream()
                .map(cartItem -> {
                    InvoiceItemModel item = new InvoiceItemModel();
                    item.setInvoiceModel(savedInvoice);
                    item.setTicketName(cartItem.getTicketType());
                    item.setTicketType(cartItem.getTicketType());
                    item.setPrice(cartItem.getPrice());
                    item.setLineID(UUID.fromString(cartItem.getLineID()));
                    item.setLineName(cartItem.getLineName());
                    item.setStartStation(cartItem.getStartStation());
                    item.setEndStation(cartItem.getEndStation());
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
     * Helper method to convert InvoiceModel to InvoiceDTO with items
     */
    private InvoiceDTO convertToInvoiceDTO(InvoiceModel invoiceModel) {
        List<InvoiceItemModel> items = invoiceItemRepository.findByInvoiceModel(invoiceModel);

        List<InvoiceItemDTO> itemDTOs = items.stream()
                .map(item -> new InvoiceItemDTO(
                        item.getInvoiceItemID(),
                        item.getTicketName(),
                        item.getTicketType(),
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
}

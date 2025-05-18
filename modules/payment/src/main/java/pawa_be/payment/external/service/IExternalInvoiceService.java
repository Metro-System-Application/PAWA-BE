package pawa_be.payment.external.service;

import org.springframework.data.domain.Page;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.payment.internal.dto.InvoiceDTO;
import pawa_be.payment.internal.dto.InvoiceItemDTO;
import pawa_be.payment.internal.dto.RequestCreateInvoiceDTO;
import pawa_be.payment.internal.dto.ResponseCreateInvoiceDTO;
import pawa_be.payment.internal.enumeration.InvoiceItemSortField;
import pawa_be.payment.internal.enumeration.TicketStatus;

import java.util.List;
import java.util.UUID;

/**
 * External interface for Invoice Service allowing access from other modules
 */
public interface IExternalInvoiceService {
    ResponseCreateInvoiceDTO createInvoice(RequestCreateInvoiceDTO requestCreateInvoiceDTO);
    InvoiceDTO getInvoiceById(UUID invoiceId);
    List<InvoiceDTO> getInvoicesByPassengerId(String passengerId);
    List<InvoiceDTO> getInvoicesByEmail(String email);
    GenericResponseDTO activateTicket(UUID invoiceItemId);
    List<InvoiceItemDTO> getInvoiceItemsByStatus(String passengerId, TicketStatus status);

    Page<InvoiceItemDTO> getInvoiceItemsPaginated(
            String passengerId,
            int page,
            int size,
            InvoiceItemSortField sortBy,
            String sortDirection);

    Page<InvoiceItemDTO> getInvoiceItemsByStatusPaginated(
            String passengerId, 
            TicketStatus status,
            int page, 
            int size, 
            InvoiceItemSortField sortBy, 
            String sortDirection);
}

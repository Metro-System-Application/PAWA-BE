package pawa_be.payment.internal.service;

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

public interface IInvoiceService {
    ResponseCreateInvoiceDTO createInvoice(RequestCreateInvoiceDTO requestCreateInvoiceDTO, String transactionId);
    InvoiceDTO getInvoiceById(UUID invoiceId);
    List<InvoiceDTO> getInvoicesByPassengerId(String passengerId);
    List<InvoiceDTO> getInvoicesByEmail(String email);
    GenericResponseDTO<InvoiceItemDTO> activateTicket(UUID invoiceItemId);
    List<InvoiceItemDTO> getMyInvoiceItemsByStatus(String passengerId, TicketStatus status);
    
    Page<InvoiceItemDTO> getInvoiceItemsPaginated(
            String passengerId, 
            int page, 
            int size, 
            InvoiceItemSortField sortBy, 
            String sortDirection);
    
    // Paginated invoice items by status
    Page<InvoiceItemDTO> getInvoiceItemsByStatusPaginated(
            String passengerId, 
            TicketStatus status,
            int page, 
            int size, 
            InvoiceItemSortField sortBy, 
            String sortDirection);
}

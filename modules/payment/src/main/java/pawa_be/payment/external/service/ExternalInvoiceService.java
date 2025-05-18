package pawa_be.payment.external.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.payment.internal.dto.InvoiceDTO;
import pawa_be.payment.internal.dto.InvoiceItemDTO;
import pawa_be.payment.internal.dto.RequestCreateInvoiceDTO;
import pawa_be.payment.internal.dto.ResponseCreateInvoiceDTO;
import pawa_be.payment.internal.enumeration.InvoiceItemSortField;
import pawa_be.payment.internal.enumeration.TicketStatus;
import pawa_be.payment.internal.service.IInvoiceService;

import java.util.List;
import java.util.UUID;

@Service
public class ExternalInvoiceService implements IExternalInvoiceService {

    @Autowired
    private IInvoiceService invoiceService;

    @Override
    public ResponseCreateInvoiceDTO createInvoice(RequestCreateInvoiceDTO requestCreateInvoiceDTO) {
        return invoiceService.createInvoice(requestCreateInvoiceDTO, null);
    }

    @Override
    public InvoiceDTO getInvoiceById(UUID invoiceId) {
        return invoiceService.getInvoiceById(invoiceId);
    }

    @Override
    public List<InvoiceDTO> getInvoicesByPassengerId(String passengerId) {
        return invoiceService.getInvoicesByPassengerId(passengerId);
    }

    @Override
    public List<InvoiceDTO> getInvoicesByEmail(String email) {
        return invoiceService.getInvoicesByEmail(email);
    }
    
    @Override
    public GenericResponseDTO activateTicket(UUID invoiceItemId) {
        return invoiceService.activateTicket(invoiceItemId);
    }
    
    @Override
    public List<InvoiceItemDTO> getInvoiceItemsByStatus(String passengerId, TicketStatus status) {
        return invoiceService.getMyInvoiceItemsByStatus(passengerId, status);
    }
    
    @Override
    public Page<InvoiceItemDTO> getInvoiceItemsPaginated(
            String passengerId, 
            int page, 
            int size, 
            InvoiceItemSortField sortBy, 
            String sortDirection) {
        return invoiceService.getInvoiceItemsPaginated(passengerId, page, size, sortBy, sortDirection);
    }
    
    @Override
    public Page<InvoiceItemDTO> getInvoiceItemsByStatusPaginated(
            String passengerId, 
            TicketStatus status, 
            int page, 
            int size, 
            InvoiceItemSortField sortBy, 
            String sortDirection) {
        return invoiceService.getInvoiceItemsByStatusPaginated(
                passengerId, status, page, size, sortBy, sortDirection);
    }
}

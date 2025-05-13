package pawa_be.payment.external.service;

import pawa_be.payment.internal.dto.InvoiceDTO;
import pawa_be.payment.internal.dto.RequestCreateInvoiceDTO;
import pawa_be.payment.internal.dto.ResponseCreateInvoiceDTO;

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
    @Deprecated
    InvoiceDTO markInvoiceAsPurchased(UUID invoiceId);
}

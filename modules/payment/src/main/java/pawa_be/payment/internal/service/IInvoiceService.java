package pawa_be.payment.internal.service;

import pawa_be.payment.internal.dto.InvoiceDTO;
import pawa_be.payment.internal.dto.RequestCreateInvoiceDTO;
import pawa_be.payment.internal.dto.ResponseCreateInvoiceDTO;

import java.util.List;
import java.util.UUID;

public interface IInvoiceService {
    ResponseCreateInvoiceDTO createInvoice(RequestCreateInvoiceDTO requestCreateInvoiceDTO, String transactionId);
    InvoiceDTO getInvoiceById(UUID invoiceId);
    List<InvoiceDTO> getInvoicesByPassengerId(String passengerId);
    List<InvoiceDTO> getInvoicesByEmail(String email);
}

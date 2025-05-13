package pawa_be.payment.external.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pawa_be.payment.internal.dto.InvoiceDTO;
import pawa_be.payment.internal.dto.RequestCreateInvoiceDTO;
import pawa_be.payment.internal.dto.ResponseCreateInvoiceDTO;
import pawa_be.payment.internal.service.InvoiceService;

import java.util.List;
import java.util.UUID;

@Service
public class ExternalInvoiceService implements IExternalInvoiceService {

    @Autowired
    private InvoiceService invoiceService;

    @Override
    public ResponseCreateInvoiceDTO createInvoice(RequestCreateInvoiceDTO requestCreateInvoiceDTO) {
        return invoiceService.createInvoice(requestCreateInvoiceDTO);
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
    public InvoiceDTO markInvoiceAsPurchased(UUID invoiceId) {
        return invoiceService.markInvoiceAsPurchased(invoiceId);
    }

    @Override
    public List<InvoiceDTO> getInvoicesByEmail(String email) {
        return invoiceService.getInvoicesByEmail(email);
    }
}

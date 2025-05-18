package pawa_be.payment.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pawa_be.infrastructure.common.dto.GenericResponseDTO;
import pawa_be.payment.internal.dto.InvoiceDTO;
import pawa_be.payment.internal.dto.InvoiceItemDTO;
import pawa_be.payment.internal.dto.RequestCreateInvoiceDTO;
import pawa_be.payment.internal.dto.RequestInvoiceByEmailDTO;
import pawa_be.payment.internal.dto.ResponseCreateInvoiceDTO;
import pawa_be.payment.internal.enumeration.TicketStatus;
import pawa_be.payment.internal.service.IInvoiceService;

import java.util.List;
import java.util.UUID;

import static pawa_be.infrastructure.jwt.misc.Miscellaneous.getUserIdFromAuthentication;

@RestController
@RequestMapping("/invoice")
@Tag(name = "Invoice Controller", description = "Operations related to invoices in the payment system")
@RequiredArgsConstructor
public class InvoiceController {

        @Autowired
        private final IInvoiceService invoiceService;

        @Operation(summary = "Create a new invoice after payment", description = "Creates a new invoice with associated items after payment is successful")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Invoice created successfully", content = @Content(schema = @Schema(implementation = ResponseCreateInvoiceDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request - invalid input"),
                        @ApiResponse(responseCode = "404", description = "Passenger not found")
        })
        @PostMapping
        public ResponseEntity<ResponseCreateInvoiceDTO> createInvoice(
                        @Valid @RequestBody RequestCreateInvoiceDTO requestCreateInvoiceDTO) {
                ResponseCreateInvoiceDTO response = invoiceService.createInvoice(requestCreateInvoiceDTO, null);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        @Operation(summary = "Get invoice by ID", description = "Retrieves an invoice by its unique identifier")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Invoice retrieved successfully", content = @Content(schema = @Schema(implementation = InvoiceDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Invoice not found")
        })
        @GetMapping("/{invoiceId}")
        public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable UUID invoiceId) {
                InvoiceDTO invoiceDTO = invoiceService.getInvoiceById(invoiceId);
                return ResponseEntity.ok(invoiceDTO);
        }

        @Operation(summary = "Get all invoices for current user", description = "Retrieves all invoices associated with the authenticated user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Invoices retrieved successfully", content = @Content(schema = @Schema(implementation = List.class)))
        })
        @GetMapping("/my-invoices")
        public ResponseEntity<List<InvoiceDTO>> getMyInvoices(Authentication authentication) {
                String passengerId = getUserIdFromAuthentication(authentication);
                List<InvoiceDTO> invoices = invoiceService.getInvoicesByPassengerId(passengerId);
                return ResponseEntity.ok(invoices);
        }

        @Operation(summary = "Get invoices by passenger ID", description = "Retrieves all invoices associated with a specific passenger ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Invoices retrieved successfully", content = @Content(schema = @Schema(implementation = List.class)))
        })
        @GetMapping("/passenger/{passengerId}")
        public ResponseEntity<List<InvoiceDTO>> getInvoicesByPassengerId(@PathVariable String passengerId) {
                List<InvoiceDTO> invoices = invoiceService.getInvoicesByPassengerId(passengerId);
                return ResponseEntity.ok(invoices);
        }

        @Operation(summary = "Get invoices by email", description = "Retrieves all invoices associated with the provided email address")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Invoices retrieved successfully", content = @Content(schema = @Schema(implementation = List.class)))
        })
        @PostMapping("/by-email")
        public ResponseEntity<List<InvoiceDTO>> getInvoicesByEmail(
                        @Valid @RequestBody RequestInvoiceByEmailDTO requestDTO) {
                List<InvoiceDTO> invoices = invoiceService.getInvoicesByEmail(requestDTO.getEmail());
                return ResponseEntity.ok(invoices);
        }

        @Operation(summary = "Activate a ticket", description = "Activates a ticket by its invoice item ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Ticket activation processed", 
                                content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Invoice item not found")
        })
        @PostMapping("/activate-ticket/{invoiceItemId}")
        public ResponseEntity<GenericResponseDTO> activateTicket(@PathVariable UUID invoiceItemId) {
                GenericResponseDTO response = invoiceService.activateTicket(invoiceItemId);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get my invoice items by status", description = "Retrieves all invoice items associated with the authenticated user filtered by status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Invoice items retrieved successfully", 
                                content = @Content(schema = @Schema(implementation = List.class)))
        })
        @GetMapping("/my-tickets/{status}")
        public ResponseEntity<List<InvoiceItemDTO>> getMyInvoiceItemsByStatus(
                Authentication authentication,
                @PathVariable("status") TicketStatus status) {
                String passengerId = getUserIdFromAuthentication(authentication);
                List<InvoiceItemDTO> items = invoiceService.getMyInvoiceItemsByStatus(passengerId, status);
                return ResponseEntity.ok(items);
        }
}

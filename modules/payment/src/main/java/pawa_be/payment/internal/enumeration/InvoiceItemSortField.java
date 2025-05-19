package pawa_be.payment.internal.enumeration;

public enum InvoiceItemSortField {
    ID("invoiceItemId"),
    TICKET_TYPE("ticketType"),
    PURCHASED_AT("purchasedAt"),
    ACTIVATED_AT("activatedAt"),
    EXPIRED_AT("expiredAt"),
    PRICE("price");

    private final String fieldName;

    InvoiceItemSortField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
} 
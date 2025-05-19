package pawa_be.ticket.external.enumerator;

public enum TicketType {
        ONE_WAY_4,
        ONE_WAY_8,
        ONE_WAY_X,
        DAILY,
        THREE_DAY,
        MONTHLY_STUDENT,
        MONTHLY_ADULT,
        FREE;

        public static TicketType fromString(String str) {
                if (str == null) {
                        throw new IllegalArgumentException("TicketType string cannot be null");
                }
                try {
                        return TicketType.valueOf(str.toUpperCase());
                } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid TicketType: " + str);
                }
        }
}

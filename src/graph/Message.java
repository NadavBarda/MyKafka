package graph;

import java.util.Date;

public class Message {
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    public Message(byte[] data) {
        this(data == null ? "" : new String(data));
    }

    public Message(String asText) {
        if (asText == null) {
            this.data = new byte[0];
            this.asText = "";
            this.asDouble = Double.NaN;
            this.date = new Date();
            return;
        }
        this.data = asText.getBytes();
        this.asText = asText;
        double parsedDouble;
        try {
            parsedDouble = Double.parseDouble(asText);
        } catch (NumberFormatException e) {
            parsedDouble = Double.NaN;
        }
        this.asDouble = parsedDouble;
        this.date = new Date();
    }

    public Message(double asDouble) {
        this(Double.toString(asDouble));
    }
}

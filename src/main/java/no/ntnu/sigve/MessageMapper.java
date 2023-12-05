package no.ntnu.sigve;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.io.InputStream;
import no.ntnu.sigve.communication.Message;

public class MessageMapper extends ObjectMapper {
    public MessageMapper() {
        super(new JsonFactory().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false));
        registerModule(new SimpleModule()
                .addSerializer(Message.class, new Message.MessageSerializer())
                .addDeserializer(Message.class, new Message.MessageDeserializer())
        );
    }

    public synchronized Message waitForMessage(InputStream is) throws IOException {
        Message message = null;
        boolean keepReading = true;
        while (keepReading) {
            try {
                message = readValue(is, Message.class);
                keepReading = message == null;
            } catch (MismatchedInputException mie) {
                // Try reading again
            }
        }
        return message;
    }
}

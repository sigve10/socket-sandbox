package no.ntnu.sigve.client;

import java.io.Serializable;
import no.ntnu.sigve.communication.Message;

public interface MessageObserver {
	void update(Message<? extends Serializable> message);
}

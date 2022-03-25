package tileworld.agent;
import sim.util.Int2D;
import tileworld.environment.TWEntity;
import java.util.Map;


public class Message {
	private String from; // the sender
	private String to; // the recepient
	private Map<Int2D, Object> message; // the message
	
	public Message(String from, String to, Map<Int2D, Object> message){
		this.from = from;
		this.to = to;
		this.message = message;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public Map<Int2D, Object> getMessage() {
		return message;
	}

}

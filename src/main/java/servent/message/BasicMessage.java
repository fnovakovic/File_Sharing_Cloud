package servent.message;


import app.AppConfig;
import app.FileInfo;
import app.ServentInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A default message implementation. This should cover most situations.
 * If you want to add stuff, remember to think about the modificator methods.
 * If you don't override the modificators, you might drop stuff.
 * @author bmilojkovic
 *
 */
public class BasicMessage implements Message {

	private static final long serialVersionUID = -9075856313609777945L;
	private final MessageType type ;
	private final ServentInfo originalSenderInfo;
	private final ServentInfo receiverInfo;
	private final List<ServentInfo> routeList;
	private final String messageText;

	private final int id;
	private final FileInfo fileInfo;
	private final String name;
	private final Map<Integer,Set<FileInfo>>updateFiles = new ConcurrentHashMap<>();

	private final Queue<String> tokenWant = new ConcurrentLinkedQueue<>();
	private final Map<String, FileInfo> files = new ConcurrentHashMap<>();

	//This gives us a unique id - incremented in every natural constructor.
	private static AtomicInteger messageCounter = new AtomicInteger(0);
	private final int messageId;
	
	public BasicMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo, Queue<String> wantToken, int id, FileInfo fileInfo, String name) {
		this.type = type;
		this.originalSenderInfo = originalSenderInfo;
		this.receiverInfo = receiverInfo;
		this.id = id;
		this.fileInfo = fileInfo;
		this.name = name;

		this.routeList = new ArrayList<>();
		this.messageText = "";
		//this.tokenWant = wantToken;
		this.tokenWant.addAll(wantToken);
		this.messageId = messageCounter.getAndIncrement();
	}
	public BasicMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo, Map<Integer,Set<FileInfo>> updateFiles, int id, FileInfo fileInfo, String name) {
		this.type = type;
		this.originalSenderInfo = originalSenderInfo;
		this.receiverInfo = receiverInfo;
		this.id = id;
		this.fileInfo = fileInfo;
		this.name = name;
		this.routeList = new ArrayList<>();
		this.messageText = "";
		this.updateFiles.putAll(updateFiles);
		this.messageId = messageCounter.getAndIncrement();
	}

	public BasicMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo, FileInfo fileInfo, String name,int id) {
		this.type = type;
		this.originalSenderInfo = originalSenderInfo;
		this.receiverInfo = receiverInfo;
		this.id = id;
		this.fileInfo = fileInfo;
		this.routeList = new ArrayList<>();
		this.messageText = "";
		this.name = name;
		this.messageId = messageCounter.getAndIncrement();
	}

//	public BasicMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo, FileInfo fileInfo, String name) {
//		this.type = type;
//		this.originalSenderInfo = originalSenderInfo;
//		this.receiverInfo = receiverInfo;
//		this.name = name;
//
//		this.routeList = new ArrayList<>();
//		this.messageText = "";
//		this.fileInfo =  new FileInfo(fileInfo.getPath(),fileInfo.getContent(),fileInfo.getName());
//		this.messageId = messageCounter.getAndIncrement();
//	}


	@Override
	public MessageType getMessageType() {
		return type;
	}

	@Override
	public ServentInfo getOriginalSenderInfo() {
		return originalSenderInfo;
	}

	@Override
	public ServentInfo getReceiverInfo() {
		return receiverInfo;
	}

	
	@Override
	public List<ServentInfo> getRoute() {
		return routeList;
	}
	
	@Override
	public String getMessageText() {
		return messageText;
	}
	
	@Override
	public int getMessageId() {
		return messageId;
	}
	
	protected BasicMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo receiverInfo,
						   List<ServentInfo> routeList, String messageText, int id, int messageId, Queue<String> wantToken, Map<Integer,Set<FileInfo>> updateFiles, FileInfo fileInfo, String name) {
		this.type = type;
		this.originalSenderInfo = originalSenderInfo;
		this.receiverInfo = receiverInfo;
		this.routeList = routeList;
		this.messageText = messageText;
		this.id = id;
		this.fileInfo = fileInfo;
		this.tokenWant.addAll(wantToken);
		this.updateFiles.putAll(updateFiles);
		this.name = name;
		this.messageId = messageId;
	}
	
	/**
	 * Used when resending a message. It will not change the original owner
	 * (so equality is not affected), but will add us to the route list, so
	 * message path can be retraced later.
	 */
	@Override
	public Message makeMeASender() {
////		ServentInfo newRouteItem = AppConfig.myServentInfo;
////
////		List<ServentInfo> newRouteList = new ArrayList<>(routeList);
////		newRouteList.add(newRouteItem);
////		Message toReturn = new BasicMessage(getMessageType(), getOriginalSenderInfo(),
////				getReceiverInfo(), isWhite(), newRouteList, getMessageText(), getMessageId());
////
//		return toReturn;
		return null;
	}
	
	/**
	 * Change the message received based on ID. The receiver has to be our neighbor.
	 * Use this when you want to send a message to multiple neighbors, or when resending.
	 */
	@Override
	public Message changeReceiver(Integer newReceiverId) {
		if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)) {
			ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);
			
			Message toReturn = new BasicMessage(getMessageType(), getOriginalSenderInfo(),
					newReceiverInfo, getRoute(), getMessageText(), getId(), getMessageId(),getTokenWant(), getUpdateFiles(), fileInfo, getName());
			
			return toReturn;
		} else {
			AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");
			
			return null;
		}
		
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}

	/**
	 * Comparing messages is based on their unique id and the original sender id.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BasicMessage) {
			BasicMessage other = (BasicMessage)obj;
			
			if (getMessageId() == other.getMessageId() &&
				getOriginalSenderInfo().getId() == other.getOriginalSenderInfo().getId()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Hash needs to mirror equals, especially if we are gonna keep this object
	 * in a set or a map. So, this is based on message id and original sender id also.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getMessageId(), getOriginalSenderInfo().getId());
	}


	@Override
	public Queue<String> getTokenWant() {
		return tokenWant;
	}

	@Override
	public Map<Integer,Set<FileInfo>>  getUpdateFiles() {
		return  updateFiles;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	/**
	 * Returns the message in the format: <code>[sender_id|message_id|text|type|receiver_id]</code>
	 */
	@Override
	public String toString() {
		return "BasicMessage{" +
				"type=" + type +
				", originalSenderInfo=" + originalSenderInfo +
				", receiverInfo=" + receiverInfo +
				", routeList=" + routeList +
				", messageText='" + messageText + '\'' +
				", id=" + id +
				", fileInfo=" + fileInfo +
				", name='" + name + '\'' +
				", updateFiles=" + updateFiles +
				", tokenWant=" + tokenWant +
				", files=" + files +
				", messageId=" + messageId +
				'}';
	}
}

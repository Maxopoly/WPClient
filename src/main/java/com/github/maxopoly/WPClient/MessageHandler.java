package com.github.maxopoly.WPClient;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.util.text.ITextComponent;

public class MessageHandler {

	private static MessageHandler instance;

	public static MessageHandler getInstance() {
		if (instance == null) {
			instance = new MessageHandler();
		}
		return instance;
	}

	private List<ITextComponent> messages;

	private MessageHandler() {
		messages = new LinkedList<ITextComponent>();
	}

	public synchronized void queueMessage(ITextComponent msg) {
		messages.add(msg);
	}

	public synchronized List<ITextComponent> popMessages() {
		if (messages.isEmpty()) {
			return new LinkedList<ITextComponent>();
		}
		List<ITextComponent> msgCopy = messages;
		messages = new LinkedList<ITextComponent>();
		return msgCopy;
	}

}

package com.allcaps;

import com.google.inject.Provides;

import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.chatbox.ChatboxInput;
import net.runelite.client.game.chatbox.ChatboxTextInput;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "AllCaps"
)
public class AllCapsPlugin extends Plugin
{

	private static final Pattern TAG_REGEXP = Pattern.compile("<[^>]*>");
	private static final Pattern WHITESPACE_REGEXP = Pattern.compile("[\\s\\u00A0]");
	private static final Pattern SLASH_REGEXP = Pattern.compile("[\\/]");
	private static final Pattern PUNCTUATION_REGEXP = Pattern.compile("[\\W\\_\\d]");

	@Inject
	private Client client;

	@Inject
	private AllCapsConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;


	@Subscribe
	public void onChatMessage(ChatMessage chatMessage) throws UnsupportedEncodingException {
		boolean isOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);

		if (client.getGameState() != GameState.LOGGED_IN  || !isOn)
		{
			return;
		}

		switch (chatMessage.getType())
		{
			case PUBLICCHAT:
			case MODCHAT:
			case FRIENDSCHAT:
			case PRIVATECHAT:
			case PRIVATECHATOUT:
			case MODPRIVATECHAT:
				break;
			default:
				return;
		}

		final MessageNode messageNode = chatMessage.getMessageNode();
		final String message = messageNode.getValue();
		final String updatedMessage = updateMessage(message);
		System.out.println(message);
		System.out.println(updatedMessage);
		if (updatedMessage == null)
		{
			return;
		}

//		ChatboxTextInput chatboxInput = new ChatboxTextInput(chatMessageManager, client);
//		chatboxInput.value(updatedMessage);
		messageNode.setRuneLiteFormatMessage(updatedMessage);
		chatMessageManager.update(messageNode);


		client.refreshChat();
	}


	@Subscribe
	public void onOverheadTextChanged(final OverheadTextChanged event) throws UnsupportedEncodingException {
		boolean isOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);

		if (client.getGameState() != GameState.LOGGED_IN  || !isOn)
		{
			return;
		}

		if (!(event.getActor() instanceof Player))
		{
			return;
		}

		final String message = event.getOverheadText();
		final String updatedMessage = updateMessage(message);

		if (updatedMessage == null)
		{
			return;
		}

		event.getActor().setOverheadText(updatedMessage);
	}

	@Provides
    AllCapsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AllCapsConfig.class);
	}


	@Nullable
	String updateMessage(final String message) throws UnsupportedEncodingException {

		final String[] splitWords = message.split(" ");

		for(int i=0; i < splitWords.length; i++){
			String newWord = "";

			for(int k=0; k < splitWords[i].length(); k++){
				char character = Character.toUpperCase(splitWords[i].charAt(k));

				byte[] b = new byte[] { (byte) Integer.parseInt("151")};
				String s = new String(b, "windows-1252");

				newWord = newWord + character + s;

			}
			splitWords[i] = newWord;
		}

		String temp = String.join(" ", splitWords);

		return temp;
	}
}

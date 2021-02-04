package org.orienteer.oposter.telegram;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.MetaDataKey;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IImageAttachment;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.oposter.model.IPosting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.ProvidedBy;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InputMedia;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.MessagesResponse;
import com.pengrad.telegrambot.response.SendResponse;

/**
 * {@link IPlatformApp} for Telegram
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = ITelegramBot.CLASS_NAME, orderOffset = 100)
public interface ITelegramBot extends IPlatformApp {
	public static final Logger LOG = LoggerFactory.getLogger(ITelegramBot.class);
	public static final String CLASS_NAME = "OPTelegramBot";
	
	@DAOField(notNull = true)
	public String getToken();
	public void setToken(String value);
	
	@Override
	public default IPosting send (IChannel channel, IContent content) {
			ITelegramChannel tChannel = checkChannelType(channel, ITelegramChannel.class);
			TelegramBot bot = getTelegramBot();
			BaseResponse response = bot.execute(prepareRequest(tChannel, content));
			Message publishedMessage = extractMessage(response);
			LOG.info("Message to generate link to: "+publishedMessage);
			if(publishedMessage!=null) {
				return IPosting.createFor(tChannel, content)
									.setExternalPostingId(publishedMessage.messageId())
									.setUrl("https://t.me/%s/%s", publishedMessage.chat().title(), publishedMessage.messageId());
			} else throw new IllegalStateException("Unknown response recieved from telegram: "+response);
	}
	
	public default BaseRequest<?, ?> prepareRequest(ITelegramChannel channel, IContent content) {
		List<IImageAttachment> images = content.getImages();
		if(images==null || images.isEmpty()) {
			return new SendMessage(channel.getTelegramChatId(), content.getContent());
		} else if(images.size()==1) {
			return new SendPhoto(channel.getTelegramChatId(), images.get(0).getData()).caption(content.getContent());
		} else {
			InputMediaPhoto[] photosToSend = new InputMediaPhoto[images.size()];
			for(int i=0; i< images.size(); i++) {
				photosToSend[i] = new InputMediaPhoto(images.get(i).getData());
			}
			photosToSend[0].caption(content.getContent());
			return new SendMediaGroup(channel.getTelegramChatId(), photosToSend);
		}
	}
	
	public default Message extractMessage(BaseResponse response) {
		if(response instanceof SendResponse) {
			return ((SendResponse)response).message();
		} else if (response instanceof MessagesResponse) {
			return ((MessagesResponse)response).messages()[0];
		}
		return null;
	}
	
	public default TelegramBot getTelegramBot() {
		String key = getMetadataKey();
		TelegramBot ret = OrienteerWebApplication.lookupApplication().getMetaData(key);
		if(ret==null) {
			ret = new TelegramBot(getToken());
			OrienteerWebApplication.lookupApplication().setMetaData(key, ret);
		}
		return ret;
	}
}

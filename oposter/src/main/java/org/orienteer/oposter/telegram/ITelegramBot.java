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

import com.google.inject.ProvidedBy;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InputMedia;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.BaseResponse;

/**
 * {@link IPlatformApp} for Telegram
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = ITelegramBot.CLASS_NAME, orderOffset = 100)
public interface ITelegramBot extends IPlatformApp {
	public static final String CLASS_NAME = "OPTelegramBot";
	
	@DAOField(notNull = true)
	public String getToken();
	public void setToken(String value);
	
	@Override
	public default boolean send (IChannel channel, IContent content) {
		if(channel instanceof ITelegramChannel) {
			TelegramBot bot = getTelegramBot();
			bot.execute(prepareRequest((ITelegramChannel)channel, content));
			return true;
		} else return false;
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

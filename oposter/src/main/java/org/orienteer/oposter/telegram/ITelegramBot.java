package org.orienteer.oposter.telegram;

import org.apache.wicket.MetaDataKey;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IPlatformApp;

import com.google.inject.ProvidedBy;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

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
			bot.execute(new SendMessage(((ITelegramChannel)channel).getTelegramChatId(), content.getContent()));
			return true;
		} else return false;
	}
	
	public default TelegramBot getTelegramBot() {
		String key = ITelegramBot.class.getSimpleName()+DAO.asDocument(this).getIdentity();
		TelegramBot ret = OrienteerWebApplication.lookupApplication().getMetaData(key);
		if(ret==null) {
			ret = new TelegramBot(getToken());
			OrienteerWebApplication.lookupApplication().setMetaData(key, ret);
		}
		return ret;
	}
}

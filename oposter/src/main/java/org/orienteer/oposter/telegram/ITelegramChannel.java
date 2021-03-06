package org.orienteer.oposter.telegram;

import org.orienteer.core.OClassDomain;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.oposter.model.IChannel;

import com.google.inject.ProvidedBy;

/**
 * {@link IChannel} for Telegram 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = ITelegramChannel.CLASS_NAME, domain = OClassDomain.SPECIFICATION, orderOffset = 100)
public interface ITelegramChannel extends IChannel {
	public static final String CLASS_NAME = "OPTelegramChannel";
	
	@DAOField(notNull = true)
	public String getTelegramChatId();
	public void setTelegramChatId(String value);
	
}

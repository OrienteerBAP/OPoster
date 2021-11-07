package org.orienteer.oposter.telegram;

import org.orienteer.core.OClassDomain;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.OrienteerOClass;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.orientdb.OrientDBProperty;

import com.google.inject.ProvidedBy;

/**
 * {@link IChannel} for Telegram 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(value = ITelegramChannel.CLASS_NAME, orderOffset = 100)
@OrienteerOClass(domain = OClassDomain.SPECIFICATION)
public interface ITelegramChannel extends IChannel {
	public static final String CLASS_NAME = "OPTelegramChannel";
	
	@OrientDBProperty(notNull = true)
	public String getTelegramChatId();
	public void setTelegramChatId(String value);
	
}

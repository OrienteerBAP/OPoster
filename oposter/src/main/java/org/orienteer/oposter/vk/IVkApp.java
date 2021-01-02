package org.orienteer.oposter.vk;

import org.apache.wicket.MetaDataKey;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.logger.OLogger;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IPlatformApp;

import com.google.inject.ProvidedBy;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.queries.wall.WallPostQuery;

import lombok.extern.slf4j.Slf4j;

@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IVkApp.CLASS_NAME, orderOffset = 100)
public interface IVkApp extends IPlatformApp {
	
	public static final MetaDataKey<VkApiClient> VK_APP_KEY = new MetaDataKey<VkApiClient>() {};
	
	public static final String CLASS_NAME = "OPVkApp";
	
	@DAOField(notNull = true)
	public Integer getAppId();
	public void setAppId(Integer value);
	
	
	@DAOField(notNull = true)
	public String getAppSecret();
	public void setAppSecret(String value);
	
	@DAOField(notNull = true)
	public String getServiceToken();
	public void setServiceToken(String value);
	
	@DAOField(notNull = true)
	public Long getDefaultUserId();
	public void setDefaultUserId(Long value);
	
	@DAOField(notNull = true)
	public String getDefaultUserAccessKey();
	public void setDefaultUserAccessKey(String value);
	
	@Override
	public default boolean send (IChannel channel, IContent content) {
		if(channel instanceof IVkWall) {
			IVkWall wall = (IVkWall) channel;
			VkApiClient vk = getVkApiClient();
			UserActor userActor = new UserActor(wall.getEffectiveUserId().intValue(), wall.getEffectiveAccessKey());
			try {
				WallPostQuery post = vk.wall().post(userActor);
				if(wall.getOwnerId()!=null) post.ownerId(wall.getOwnerId().intValue());
				post.message(content.getContent());
				String ret = post.executeAsString();
				System.out.println("VK return: "+ret);
				return true;
			} catch (ClientException e) {
				OLogger.log(e, DAO.asDocument(channel).getIdentity().toString());
			}
		}
		return false;
	}
	
	public default VkApiClient getVkApiClient() {
		VkApiClient ret = OrienteerWebApplication.lookupApplication().getMetaData(VK_APP_KEY);
		if(ret==null) {
			TransportClient transportClient = new HttpTransportClient();
			ret = new VkApiClient(transportClient);
			OrienteerWebApplication.lookupApplication().setMetaData(VK_APP_KEY, ret);
		}
		return ret;
	}
}

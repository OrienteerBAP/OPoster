package org.orienteer.oposter.ok;

import org.apache.logging.log4j.util.Strings;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.logger.OLogger;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IOAuthReciever;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.oposter.web.OAuthCallbackResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.github.scribejava.apis.OdnoklassnikiApi;
import com.github.scribejava.apis.VkontakteApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * {@link IPlatformApp} for Odnoklassniki 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IOkApp.CLASS_NAME, orderOffset = 100)
public interface IOkApp extends IPlatformApp {
	public static final Logger LOG = LoggerFactory.getLogger(IOkApp.class);
	public static final String CLASS_NAME = "OPOkApp";
	
	@DAOField(notNull = true)
	public Long getAppId();
	public void setAppId(Long value);
	
	@DAOField(notNull = true)
	public String getPublicKey();
	public void setPublicKey(String value);
	
	@DAOField(notNull = true, visualization = UIVisualizersRegistry.VISUALIZER_PASSWORD)
	public String getSecretKey();
	public void setSecretKey(String value);
	
	@Override
	public default boolean send(IChannel channel, IContent content) {
		if(channel instanceof IOkChannel) {
			IOkChannel account = (IOkChannel) channel;
			try(OAuth20Service service = getService(null)) {
				String publicKey = getPublicKey();
				final OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.ok.ru/api/mediatopic/post");
				request.addQuerystringParameter("application_key", publicKey);
				String groupId = account.getGroupId();
				if(!Strings.isEmpty(groupId)) {
					request.addQuerystringParameter("gid", groupId);
					request.addQuerystringParameter("type", "GROUP_THEME");
				} else {
					request.addQuerystringParameter("type", "USER");
				}
				JSONObject attachment = new JSONObject();
				JSONObject text = new JSONObject();
				text.put("type", "text");
				text.put("text", content.getContent());
				attachment.put("media", new JSONArray().put(0, text));
				request.addBodyParameter("attachment", attachment.toString());
		        service.signRequest(account.getAccessToken(), request);
				Response response = service.execute(request);
				response.getBody();//Make sure that body is parsed
				LOG.info("Response is: "+response);
			} catch (Exception ex) {
				OLogger.log(ex);
			}
		}
		return false;
	}
	public default OAuth20Service getService(IOAuthReciever reciever) {
		
		ODocument reciverDoc = DAO.asDocument(reciever!=null?reciever:this); 
		return new ServiceBuilder(getAppId().toString())
	                .apiSecret(getSecretKey())
	                .defaultScope("PUBLISH_TO_STREAM;VALUABLE_ACCESS;LONG_ACCESS_TOKEN;PHOTO_CONTENT;GROUP_CONTENT") // replace with desired scope
	                .callback(OAuthCallbackResource.urlFor(reciverDoc))
	                .build(OdnoklassnikiApi.instance());
	}
	
}

package org.orienteer.oposter.twitter;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.WicketRuntimeException;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.oposter.web.OAuthCallbackResource;

import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.signature.TwitterCredentials;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.common.base.Strings;
import com.google.inject.ProvidedBy;

@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = ITwitterApp.CLASS_NAME, orderOffset = 100)
public interface ITwitterApp extends IPlatformApp {
	public static final String CLASS_NAME = "OPTwitterApp";
	
	@DAOField(notNull = true)
	public String getApiKey();
	public void setApiKey(String value);
	
	@DAOField(notNull = true, visualization = UIVisualizersRegistry.VISUALIZER_PASSWORD)
	public String getApiSecretKey();
	public void setApiSecretKey(String value);
	

	@Override
	public default boolean send(IChannel channel, IContent content) {
		if(channel instanceof ITwitterAccount) {
			ITwitterAccount account = (ITwitterAccount) channel;
			String accessToken = account.getAccessToken();
			String accessTokenSecret = account.getAccessTokenSecret();
			if(Strings.isNullOrEmpty(accessToken) || Strings.isNullOrEmpty(accessTokenSecret)) 
				throw new WicketRuntimeException("Please authentificate first with twitter");
			TwitterClient client = new TwitterClient(
											new TwitterCredentials(getApiKey(),
																   getApiSecretKey(),
																   accessToken,
																   accessTokenSecret));
			client.postTweet(content.getContent());
			return true;
		}
		return false;
	}
	
	public default OAuth10aService getOAuthService(ITwitterAccount account) {
			return new ServiceBuilder(getApiKey())
							.apiSecret(getApiSecretKey())
							.callback(OAuthCallbackResource.urlFor(account))
							.build(TwitterApi.instance());
	}
	
	
}

package org.orienteer.oposter.twitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.crypt.StringUtils;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.logger.OLogger;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IImageAttachment;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.oposter.web.OAuthCallbackResource;

import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.dto.tweet.MediaCategory;
import com.github.redouane59.twitter.signature.TwitterCredentials;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.common.base.Joiner;
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
			if(content.hasImages()) {
				List<IImageAttachment> images = content.getImages();
				List<String> mediaIds = new ArrayList<String>();
				try {
					for(int i=0; i<images.size() && i<4; i++) { //Twitter supports only 4 images
						mediaIds.add(client.uploadMedia(images.get(i).asFile(), MediaCategory.TWEET_IMAGE).getMediaId());
					}
				} catch (IOException e) {
					//Just log and allow to send whatever was already uploaded
					OLogger.log(e, DAO.asDocument(channel).getIdentity().toString());
				}
				client.postTweet(content.getContent(), null, Joiner.on(',').join(mediaIds));
			} else {
				client.postTweet(content.getContent());
			}
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

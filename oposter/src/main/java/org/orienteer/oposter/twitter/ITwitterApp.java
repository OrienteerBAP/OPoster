package org.orienteer.oposter.twitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.crypt.StringUtils;
import org.orienteer.core.OClassDomain;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.OrienteerOClass;
import org.orienteer.core.dao.OrienteerOProperty;
import org.orienteer.logger.OLogger;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IImageAttachment;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.oposter.model.IPosting;
import org.orienteer.oposter.web.OAuthCallbackResource;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.orientdb.OrientDBProperty;

import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.dto.tweet.MediaCategory;
import com.github.redouane59.twitter.dto.tweet.Tweet;
import com.github.redouane59.twitter.signature.TwitterCredentials;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.ProvidedBy;

/**
 * {@link IPlatformApp} for Twitter. Corresponding application should be created and than 
 * key and secret key configured on app document
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(value = ITwitterApp.CLASS_NAME, orderOffset = 100)
@OrienteerOClass(domain = OClassDomain.SPECIFICATION)
public interface ITwitterApp extends IPlatformApp {
	public static final String CLASS_NAME = "OPTwitterApp";
	
	@OrientDBProperty(notNull = true)
	public String getApiKey();
	public void setApiKey(String value);
	
	@OrientDBProperty(notNull = true)
	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_PASSWORD)
	public String getApiSecretKey();
	public void setApiSecretKey(String value);
	

	@Override
	public default IPosting send(IChannel channel, IContent content) throws Exception {
		ITwitterAccount account = checkChannelType(channel, ITwitterAccount.class);
		String accessToken = account.getAccessToken();
		String accessTokenSecret = account.getAccessTokenSecret();
		if(Strings.isNullOrEmpty(accessToken) || Strings.isNullOrEmpty(accessTokenSecret)) 
			throw new WicketRuntimeException("Please authentificate first with twitter");
		TwitterClient client = new TwitterClient(
										new TwitterCredentials(getApiKey(),
															   getApiSecretKey(),
															   accessToken,
															   accessTokenSecret));
		Tweet tweet;
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
			tweet = client.postTweet(content.getContent(), null, Joiner.on(',').join(mediaIds));
		} else {
			tweet = client.postTweet(content.getContent());
		}
		return IPosting.createFor(channel, content)
							.setExternalPostingId(tweet.getId())
							.setUrl("https://twitter.com/%s/status/%s", tweet.getAuthorId(), tweet.getId());
	}
	
	public default OAuth10aService getOAuthService(ITwitterAccount account) {
			return new ServiceBuilder(getApiKey())
							.apiSecret(getApiSecretKey())
							.callback(OAuthCallbackResource.urlFor(account))
							.build(TwitterApi.instance());
	}
	
	
}

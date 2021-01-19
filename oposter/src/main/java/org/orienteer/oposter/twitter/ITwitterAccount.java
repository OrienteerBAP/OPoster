package org.orienteer.oposter.twitter;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.http.WebRequest;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.method.IMethodContext;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.core.method.filters.WidgetTypeFilter;
import org.orienteer.core.util.CommonUtils;
import org.orienteer.core.web.ODocumentPage;
import org.orienteer.logger.OLogger;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IOAuthReciever;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.inject.ProvidedBy;

@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = ITwitterAccount.CLASS_NAME, orderOffset = 100)
public interface ITwitterAccount extends IChannel, IOAuthReciever {
	public static final String CLASS_NAME = "OPTwitterAccount";
	public static final MetaDataKey<OAuth1RequestToken> REQUEST_TOKEN_KEY = new MetaDataKey<OAuth1RequestToken>() {};
	
	public String getAccessToken();
	public void setAccessToken(String value);
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_PASSWORD)
	public String getAccessTokenSecret();
	public void setAccessTokenSecret(String value);
	
	@OMethod(
			titleKey = "command.connectoauth", 
			order=10,bootstrap=BootstrapType.SUCCESS,icon = FAIconType.play,
			filters={
					@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
					@OFilter(fClass = WidgetTypeFilter.class, fData = "parameters"),
			}
	)
	public default void connectOAuth(IMethodContext ctx) {
		if(getPlatformApp() instanceof ITwitterApp) {
			ITwitterApp twitterApp = (ITwitterApp) getPlatformApp();
			OAuth10aService service = twitterApp.getOAuthService(this);
			OAuth1RequestToken requestToken;
			try {
				requestToken = service.getRequestToken();
			} catch (Exception e) {
				ctx.showFeedback(FeedbackMessage.ERROR, "error.oauthrequest", Model.of(e.getMessage()));
				OLogger.log(e, DAO.asDocument(this).getIdentity().toString());
				return;
			}
			String redirectTo = service.getAuthorizationUrl(requestToken);
			OrienteerWebSession.get().setMetaData(REQUEST_TOKEN_KEY, requestToken);
			throw new RedirectToUrlException(redirectTo);
			
		} else {
			ctx.showFeedback(FeedbackMessage.ERROR, "error.wrongplatform", Model.of(ITwitterApp.class));
		}
	}
	@Override
	public default void callback(WebRequest request, ODocumentPage targetPage) throws Exception {
		if(getPlatformApp() instanceof ITwitterApp) {
			ITwitterApp twitterApp = (ITwitterApp) getPlatformApp();
			OAuth10aService service = twitterApp.getOAuthService(this);
			OAuth1RequestToken requestToken = OrienteerWebSession.get().getMetaData(REQUEST_TOKEN_KEY);
			String oauthVerifier = request.getRequestParameters().getParameterValue("oauth_verifier").toString();
			OAuth1AccessToken token = service.getAccessToken(requestToken, oauthVerifier);
			setAccessToken(token.getToken());
			setAccessTokenSecret(token.getTokenSecret());
			DAO.save(this);
		} else {
			targetPage.error(CommonUtils.localize("error.wrongplatform", "simpleName", ITwitterApp.class.getSimpleName()));
		}
	}
	
	
	
}

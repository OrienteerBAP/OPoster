package org.orienteer.oposter.facebook;

import org.apache.wicket.request.flow.RedirectToUrlException;
import org.orienteer.core.OClassDomain;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.OrienteerOClass;
import org.orienteer.core.dao.OrienteerOProperty;
import org.orienteer.core.method.IMethodContext;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.core.method.filters.WidgetTypeFilter;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IOAuthReciever;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.oposter.web.OAuthCallbackResource;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.orientdb.OrientDBProperty;

import com.google.inject.ProvidedBy;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Parameter;
import com.restfb.scope.FacebookPermissions;
import com.restfb.scope.ScopeBuilder;
import com.restfb.types.Page;

/**
 * {@link IChannel} for Facebook
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(value = IFacebookConnection.CLASS_NAME, orderOffset = 100)
@OrienteerOClass(domain = OClassDomain.SPECIFICATION)
public interface IFacebookConnection extends IChannel, IOAuthReciever{
	public static final String CLASS_NAME = "OPFacebookConnection";
	
	@OrientDBProperty(notNull = true)
	public Long getFacebookId();
	public void setFacebookId(Long value);
	
	public default String getFacebookIdAsString() {
		Long pageId = getFacebookId();
		return pageId!=null?Long.toUnsignedString(pageId):null;
	}
	
	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_PASSWORD)
	public String getAccessToken();
	public void setAccessToken(String value);
	
	@OrientDBProperty(notNull = true, defaultValue = "false")
	public boolean isPage();
	public void setPage(boolean value);
	
	
	@OMethod(
			titleKey = "command.connectoauth", 
			order=10,bootstrap=BootstrapType.SUCCESS,icon = FAIconType.play,
			filters={
					@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
					@OFilter(fClass = WidgetTypeFilter.class, fData = "parameters"),
			}
	)
	public default void connectOAuth(IMethodContext ctx) {
		IPlatformApp app = getPlatformApp();
		if(app instanceof IFacebookApp) {
			IFacebookApp fbApp = (IFacebookApp) app;
			ScopeBuilder scope = new ScopeBuilder();
			scope.addPermission(isPage()?FacebookPermissions.PAGES_MANAGE_POSTS:FacebookPermissions.PUBLISH_TO_GROUPS);
			String redirectTo = fbApp.getFacebookClient().getLoginDialogUrl(fbApp.getAppId(),
					OAuthCallbackResource.urlFor(DAO.asDocument(this)), 
					scope);
			throw new RedirectToUrlException(redirectTo);
		}
	}
	
	@Override
	public default void codeObtained(String code) throws Exception {
		IPlatformApp app = getPlatformApp();
		if(app instanceof IFacebookApp) {
			IFacebookApp fbApp = (IFacebookApp) app;
			FacebookClient facebookClient = fbApp.getFacebookClient();
			AccessToken token = facebookClient.obtainUserAccessToken(fbApp.getAppId(), 
															   fbApp.getAppSecret(),
															   OAuthCallbackResource.urlFor(DAO.asDocument(this)),
															   code);
			if(token.getExpires()!=null) {
				token = facebookClient.obtainExtendedAccessToken(fbApp.getAppId(), fbApp.getAppSecret(), token.getAccessToken());
			}
			String accessToken = token.getAccessToken();
			if(isPage()) {
				facebookClient = facebookClient.createClientWithAccessToken(token.getAccessToken());
				Page page = facebookClient.fetchObject(getFacebookIdAsString(), Page.class, Parameter.with("fields","access_token"));
				accessToken = page.getAccessToken();
			}
			setAccessToken(accessToken);
			DAO.save(this);
		}
	}
	
}

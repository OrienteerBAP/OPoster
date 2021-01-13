package org.orienteer.oposter.facebook;

import org.apache.wicket.request.flow.RedirectToUrlException;
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
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IOAuthReciever;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.oposter.web.OAuthCallbackResource;

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
@DAOOClass(value = IFacebookPage.CLASS_NAME, orderOffset = 100)
public interface IFacebookPage extends IChannel, IOAuthReciever{
	public static final String CLASS_NAME = "OPFacebookPage";
	
	@DAOField(notNull = true)
	public Long getPageId();
	public void setPageId(Long value);
	
	public default String getPageIdAsString() {
		Long pageId = getPageId();
		return pageId!=null?Long.toUnsignedString(pageId):null;
	}
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_PASSWORD)
	public String getPageAccessToken();
	public void setPageAccessToken(String value);
	
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
			scope.addPermission(FacebookPermissions.PAGES_MANAGE_POSTS);
			String redirectTo = fbApp.getFacebookClient().getLoginDialogUrl(fbApp.getAppId(),
					OAuthCallbackResource.urlFor(DAO.asDocument(this)).toString(), 
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
															   OAuthCallbackResource.urlFor(DAO.asDocument(this))
															   		.toString(),
															   code);
			if(token.getExpires()!=null) {
				token = facebookClient.obtainExtendedAccessToken(fbApp.getAppId(), fbApp.getAppSecret(), token.getAccessToken());
			}
			facebookClient = facebookClient.createClientWithAccessToken(token.getAccessToken());
			Page page = facebookClient.fetchObject(getPageIdAsString(), Page.class, Parameter.with("fields","access_token"));
			setPageAccessToken(page.getAccessToken());
			DAO.save(this);
		}
	}
	
}

package org.orienteer.oposter.ok;

import java.io.IOException;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.method.IMethodContext;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.core.method.filters.WidgetTypeFilter;
import org.orienteer.logger.OLogger;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IOAuthReciever;
import org.orienteer.oposter.model.IPlatformApp;

import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.inject.ProvidedBy;

/**
 * {@link IChannel} which user, group or page in Odnoklassniki
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IOkChannel.CLASS_NAME, orderOffset = 100)
public interface IOkChannel extends IChannel, IOAuthReciever {
	public static final String CLASS_NAME = "OPOkChannel";
	
	public String getGroupId();
	public void setGroupId(String value);
	
	public String getAccessToken();
	public void setAccessToken(String value);
	
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
		if(app instanceof IOkApp) {
			IOkApp okApp = (IOkApp) app;
			OAuth20Service service = okApp.getService(this);
			String redirectTo = service.getAuthorizationUrl();
			throw new RedirectToUrlException(redirectTo);
		}
	}
	
	@Override
	public default void codeObtained(String code) throws Exception {
		IPlatformApp app = getPlatformApp();
		if(app instanceof IOkApp) {
			IOkApp okApp = (IOkApp) app;
			try(OAuth20Service service = okApp.getService(this)) {
				setAccessToken(service.getAccessToken(code).getAccessToken());
				DAO.save(this);
			}
		}
	}
	
	
}

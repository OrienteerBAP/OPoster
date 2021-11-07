package org.orienteer.oposter.vk;

import java.io.IOException;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.orienteer.core.OClassDomain;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.OrienteerOClass;
import org.orienteer.core.method.IMethodContext;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.core.method.filters.WidgetTypeFilter;
import org.orienteer.core.util.CommonUtils;
import org.orienteer.logger.OLogger;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IOAuthReciever;
import org.orienteer.oposter.model.IPlatformApp;
import org.orienteer.transponder.annotation.DefaultValue;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.orientdb.OrientDBProperty;

import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.common.base.Throwables;
import com.google.inject.ProvidedBy;
import com.vk.api.sdk.client.actors.UserActor;

/**
 * {@link IChannel} for VKontakte 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(value = IVkWall.CLASS_NAME, orderOffset = 100)
@OrienteerOClass(domain = OClassDomain.SPECIFICATION)
public interface IVkWall extends IChannel, IOAuthReciever {
	public static final String CLASS_NAME = "OPVkWall";
	
	public Long getOwnerId();
	public void setOwnerId(Long value);
	
	@OrientDBProperty(defaultValue = "true")
	@DefaultValue("true")
	public boolean isCommunity();
	public void setCommunity(boolean value);
	
	public Long getUserId();
	public void setUserId(Long value);
	
	public String getUserAccessKey();
	public void setUserAccessKey(String value);
	
	public default Long getAdjustedOwnerId() {
		Long ownerId = getOwnerId();
		if(ownerId!=null && isCommunity()) return -ownerId;
		else return ownerId;
	}
	
	public default Long getEffectiveUserId() {
		Long userId = getUserId();
		if(userId!=null) return userId;
		IPlatformApp app = getPlatformApp();
		if(app!=null && app instanceof IVkApp) return ((IVkApp)app).getDefaultUserId();
		else return null;
	}
	
	public default String getEffectiveAccessKey( ) {
		String userAccessKey = getUserAccessKey();
		if(userAccessKey!=null) return userAccessKey;
		IPlatformApp app = getPlatformApp();
		if(app!=null && app instanceof IVkApp) return ((IVkApp)app).getDefaultUserAccessKey();
		else return null;
	}
	
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
		if(app instanceof IVkApp) {
			IVkApp vkApp = (IVkApp) app;
			try(OAuth20Service service = vkApp.getService(this)) {
				String redirectTo = service.getAuthorizationUrl();
				/*String redirectTo;
				if(getOwnerId()!=null && Boolean.TRUE.equals(isCommunity())) {
					redirectTo = service.getAuthorizationUrl(CommonUtils.toMap("group_ids",getOwnerId().toString()));
				} else {
					redirectTo = service.getAuthorizationUrl();
				}*/
				throw new RedirectToUrlException(redirectTo);
			} catch (IOException e) {
				ctx.showFeedback(FeedbackMessage.ERROR, "error.oauthrequest", Model.of(e.getMessage()));
				OLogger.log(e, DAO.asDocument(this).getIdentity().toString());
			}
		}
	}
	
	@Override
	public default void codeObtained(String code) throws Exception {
		IPlatformApp app = getPlatformApp();
		if(app instanceof IVkApp) {
			IVkApp vkApp = (IVkApp) app;
			try(OAuth20Service service = vkApp.getService(this)) {
				setUserAccessKey(service.getAccessToken(code).getAccessToken());
				setUserId(vkApp.getVkApiClient().users().get(
								new UserActor(null, getUserAccessKey())).execute().get(0).getId().longValue());
				DAO.save(this);
			}
		}
	}
}

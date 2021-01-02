package org.orienteer.oposter.vk;

import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IPlatformApp;

import com.google.inject.ProvidedBy;

@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IVkWall.CLASS_NAME, orderOffset = 100)
public interface IVkWall extends IChannel {
	public static final String CLASS_NAME = "OPVkWall";
	
	public Long getOwnerId();
	public void setOwnerId(Long value);
	
	@DAOField(defaultValue = "true")
	public Boolean isCommunity();
	public void setCommunity(Boolean value);
	
	public Long getUserId();
	public void setUserId(Long value);
	
	public String getUserAccessKey();
	public void setUserAccessKey(String value);
	
	public default Long getAdjustedOwnerId() {
		Long ownerId = getOwnerId();
		if(ownerId!=null && Boolean.TRUE.equals(isCommunity())) return -ownerId;
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
	
}

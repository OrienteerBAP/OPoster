package org.orienteer.oposter.facebook;

import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.oposter.model.IChannel;

import com.google.inject.ProvidedBy;

/**
 * {@link IChannel} for Facebook
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IFacebookPage.CLASS_NAME, orderOffset = 100)
public interface IFacebookPage extends IChannel{
	public static final String CLASS_NAME = "OPFacebookPage";
	
	@DAOField(notNull = true)
	public Long getPageId();
	public void setPageId(Long value);
	
	public default String getPageIdAsString() {
		Long pageId = getPageId();
		return pageId!=null?Long.toUnsignedString(pageId):null;
	}
	
	@DAOField(notNull = true, visualization = UIVisualizersRegistry.VISUALIZER_PASSWORD)
	public String getPageAccessToken();
	public void setPageAccessToken(String value);
	
}

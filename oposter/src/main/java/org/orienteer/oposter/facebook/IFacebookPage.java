package org.orienteer.oposter.facebook;

import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.oposter.model.IChannel;

import com.google.inject.ProvidedBy;

@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IFacebookPage.CLASS_NAME, orderOffset = 100)
public interface IFacebookPage extends IChannel{
	public static final String CLASS_NAME = "OPFacebookChannel";
	
	@DAOField(notNull = true)
	public String getConnection();
	public void setConnection(String value);
	
	@DAOField(notNull = true, visualization = UIVisualizersRegistry.VISUALIZER_PASSWORD)
	public String getPageAccessToken();
	public void setPageAccessToken(String value);
	
}

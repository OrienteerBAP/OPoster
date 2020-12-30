package org.orienteer.oposter.model;

import java.util.List;

import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;

import com.google.inject.ProvidedBy;

@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IChannel.CLASS_NAME, isAbstract = true, parentProperty = "platformApp")
public interface IChannel {
	public static final String CLASS_NAME = "OPChannel";
	
	public String getName();
	public void setName(String name);
	
	public String getUrl();
	public void setUrl(String value);
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_TEXTAREA)
	public String getDescription();
	public void setDescription(String value);
	
	@DAOField(inverse = "channels", notNull = true, visualization = UIVisualizersRegistry.VISUALIZER_LISTBOX)
	public IPlatformApp getPlatformApp();
	public void setPlatformApp(IPlatformApp value);
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_TABLE, inverse = "channels")
	public List<IContent> getContent();
	public void setContent(List<IContent> value);
	
	public default void send(IContent content) {
		IPlatformApp  platformApp = getPlatformApp();
		if(platformApp!=null) platformApp.send(this, content);
		else throw new IllegalStateException("Please define Platform App first");
	}
	
}

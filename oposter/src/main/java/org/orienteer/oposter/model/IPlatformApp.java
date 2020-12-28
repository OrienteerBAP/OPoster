package org.orienteer.oposter.model;

import java.util.List;

import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;

import com.google.inject.ProvidedBy;

@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IPlatformApp.CLASS_NAME, isAbstract = true)
public interface IPlatformApp {
	public static final String CLASS_NAME = "OPPlatformApp";
	
	public String getName();
	public void setName(String name);
	
	public String getDescription();
	public void setDescription(String value);
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_TABLE, inverse = "platformApp")
	public List<IChannel> getChannels();
	public void setChannels(List<IChannel> value);
	
}

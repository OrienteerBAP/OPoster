package org.orienteer.oposter.model;

import java.util.List;

import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;

import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Class which holds configuration about connectivity to some social media
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IPlatformApp.CLASS_NAME,
           isAbstract = true,
           displayable = {"name", "description"})
public interface IPlatformApp {
	public static final String CLASS_NAME = "OPPlatformApp";
	
	public String getName();
	public void setName(String name);
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_TEXTAREA)
	public String getDescription();
	public void setDescription(String value);
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_TABLE, inverse = "platformApp")
	public List<IChannel> getChannels();
	public void setChannels(List<IChannel> value);
	
	public default boolean send (IChannel channel, IContent content) {
		throw new IllegalStateException("Child classes should override this default method");
	}
	
	public default String getMetadataKey() {
		ODocument doc = DAO.asDocument(this);
		return doc.getSchemaClass().getName()+doc.getIdentity()+doc.getVersion();
	}
	
}

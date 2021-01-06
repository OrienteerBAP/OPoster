package org.orienteer.oposter.model;

import java.util.List;

import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;

import com.google.inject.ProvidedBy;

/**
 * Content Plan - way to organize content 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value=IContentPlan.CLASS_NAME,
		   displayable = {"name", "description"})
public interface IContentPlan {
	public static final String CLASS_NAME = "OPContentPlan";
	
	public String getName();
	public void setName(String name);
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_TEXTAREA)
	public String getDescription();
	public void setDescription(String value);
	
	
	@DAOField(inverse = "contentPlan", visualization = UIVisualizersRegistry.VISUALIZER_TABLE)
	public List<IContent> getContent();
	public void setContent(List<IContent> content);
}

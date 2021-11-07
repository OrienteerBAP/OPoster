package org.orienteer.oposter.model;

import java.util.List;

import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.OrienteerOClass;
import org.orienteer.core.dao.OrienteerOProperty;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.EntityType;

import com.google.inject.ProvidedBy;

/**
 * Content Plan - way to organize content 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(IContentPlan.CLASS_NAME)
@OrienteerOClass(displayable = {"name", "description"})
public interface IContentPlan {
	public static final String CLASS_NAME = "OPContentPlan";
	
	public String getName();
	public void setName(String name);
	
	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_TEXTAREA)
	public String getDescription();
	public void setDescription(String value);
	
	@EntityProperty(inverse = "contentPlan")
	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_TABLE)
	public List<IContent> getContent();
	public void setContent(List<IContent> content);
}

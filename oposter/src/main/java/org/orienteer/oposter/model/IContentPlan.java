package org.orienteer.oposter.model;

import java.util.List;

import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;

import com.google.inject.ProvidedBy;

@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(IContentPlan.CLASS_NAME)
public interface IContentPlan {
	public static final String CLASS_NAME = "OPContentPlan";
	
	public String getName();
	public void setName(String name);
	
	@DAOField(inverse = "contentPlan", visualization = UIVisualizersRegistry.VISUALIZER_TABLE)
	public List<IContent> getContent();
	public void setContent(List<IContent> content);
}

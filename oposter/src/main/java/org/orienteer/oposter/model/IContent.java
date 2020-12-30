package org.orienteer.oposter.model;

import java.util.Date;
import java.util.List;

import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.oposter.component.attachment.AttachmentsVisualizer;

import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.metadata.schema.OType;

@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IContent.CLASS_NAME, 
		   parentProperty = "contentPlan",
		   displayable = {"title", "when", "published", "created", "contentPlan"})
public interface IContent {
	public static final String CLASS_NAME = "OPContent";
	
	public String getTitle();
	public void setTitle(String title);
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_TEXTAREA)
	public String getContent();
	public void setContent(String value);
	
	@DAOField(type = OType.DATETIME)
	public Date getWhen();
	public void setWhen(Date value);
	
	@DAOField(defaultValue = "false")
	public Boolean isPublished();
	public void setPublished(Boolean published);
	
	@DAOField(type = OType.DATETIME, defaultValue = "sysdate()", readOnly = true)
	public Date getCreated();
	public void setCreated(Date value);
	
	
	public default void published() {
		setPublished(true);
	}
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_SUGGEST, inverse = "content")
	public List<IChannel> getChannels();
	public void setChannels(List<IChannel> value);
	
	@DAOField(inverse = "content")
	public IContentPlan getContentPlan();
	public void setContentPlan(IContentPlan value);

	@DAOField(inverse = "content", visualization = AttachmentsVisualizer.NAME)
	public List<IImageAttachment> getImages();
	public void setImages(List<IImageAttachment> value);
	
	
	
}

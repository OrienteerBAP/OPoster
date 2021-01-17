package org.orienteer.oposter.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.model.Model;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.method.IMethodContext;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.core.method.filters.WidgetTypeFilter;
import org.orienteer.oposter.component.attachment.AttachmentsVisualizer;

import com.google.common.base.Throwables;
import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Content to be distributed to channels
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IContent.CLASS_NAME, 
		   parentProperty = "contentPlan",
		   displayable = {"title", "when", "published", "created", "contentPlan", "channels"})
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
	
	public default boolean hasImages() {
		List<IImageAttachment> images = getImages();
		return images!=null && !images.isEmpty();
	}
	
	@OMethod(
			titleKey = "content.sendnow", 
			order=10,bootstrap=BootstrapType.SUCCESS,icon = FAIconType.play,
			filters={
					@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
					@OFilter(fClass = WidgetTypeFilter.class, fData = "parameters"),
			}
	)
	public default void sendNow(IMethodContext ctx) {
		List<IChannel> channels = getChannels();
		if(channels==null || channels.isEmpty()) {
			ctx.showFeedback(FeedbackMessage.WARNING, "content.warning.nochannels", null);
		} else {
			int errors = 0;
			for (IChannel iChannel : channels) {
				try {
					iChannel.send(this);
				} catch (Throwable e) {
					errors++;
					ctx.showFeedback(FeedbackMessage.ERROR, "content.error.cantsend", Model.of(Throwables.getStackTraceAsString(e)));
				}
			}
			if(errors==0) ctx.showFeedback(FeedbackMessage.INFO, "content.info.wassent", null);
		}
	}
	
}

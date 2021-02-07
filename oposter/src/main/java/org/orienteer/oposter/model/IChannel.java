package org.orienteer.oposter.model;

import java.util.List;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.model.Model;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.method.IMethodContext;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.core.method.filters.WidgetTypeFilter;
import org.orienteer.logger.OLogger;

import com.google.common.base.Throwables;
import com.google.inject.ProvidedBy;

/**
 * Channel to send content to 
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IChannel.CLASS_NAME, 
           isAbstract = true,
           parentProperty = "platformApp",
           displayable = {"name", "url", "description", "platformApp"})
public interface IChannel {
	public static final String CLASS_NAME = "OPChannel";
	
	public String getName();
	public void setName(String name);
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_URL_LINK)
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
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_TABLE, inverse = "channel")
	public List<IPosting> getPostings();
	public void setPostings(List<IPosting> value);
	
	@OMethod(
			titleKey = "channel.testsend", 
			order=10,bootstrap=BootstrapType.SUCCESS,icon = FAIconType.play,
			filters={
					@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
					@OFilter(fClass = WidgetTypeFilter.class, fData = "parameters"),
			}
	)
	public default void testSend(IMethodContext ctx) {
		IPlatformApp platformApp = getPlatformApp();
		if(platformApp==null) ctx.showFeedback(FeedbackMessage.ERROR, "channel.error.noplatformapp", null);
		else {
			try {
				IContent content = DAO.create(IContent.class);
				content.setTitle("Test Content for channel "+getName());
				content.setContent("This is test content for channel "+getName());
				platformApp.send(this, content);
				ctx.showFeedback(FeedbackMessage.INFO, "channel.info.testwassent", null);
			} catch (Exception e) {
				Throwable rootCause = Throwables.getRootCause(e);
				ctx.showFeedback(FeedbackMessage.ERROR, "content.error.cantsend", Model.of(rootCause.getMessage()));
				OLogger.log(rootCause, DAO.asDocument(this).getIdentity().toString());
			}
		}
	}
	
}

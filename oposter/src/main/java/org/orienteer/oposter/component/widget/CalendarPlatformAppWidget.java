package org.orienteer.oposter.component.widget;

import java.util.Date;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.orienteer.core.widget.Widget;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IContentPlan;
import org.orienteer.oposter.model.IPlatformApp;

import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Calendar widget for content to be sent to particular platform app
 */
@Widget(id="cal-platform-app", domain="document", tab = "calendar", selector=IPlatformApp.CLASS_NAME, order=10, autoEnable=true)
public class CalendarPlatformAppWidget extends AbstractCalendarContentWidget<ODocument> {

	public CalendarPlatformAppWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
	}

	@Override
	public List<IContent> lookupContent(Date start, Date end) {
		return dao.findContentByPlatformApp(getModelObject(), start, end);
	}

}

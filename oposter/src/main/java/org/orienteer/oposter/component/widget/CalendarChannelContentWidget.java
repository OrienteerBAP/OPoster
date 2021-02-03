package org.orienteer.oposter.component.widget;

import java.util.Date;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.orienteer.core.widget.Widget;
import org.orienteer.oposter.model.IChannel;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IContentPlan;

import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Calendar widget for content on particular channel
 */
@Widget(id="cal-channel", domain="document", tab = "calendar", selector=IChannel.CLASS_NAME, order=10, autoEnable=true)
public class CalendarChannelContentWidget extends AbstractCalendarContentWidget<ODocument> {

	public CalendarChannelContentWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
	}

	@Override
	public List<IContent> lookupContent(Date start, Date end) {
		return dao.findContentByChannel(getModelObject(), start, end);
	}

}

package org.orienteer.oposter.component.widget;

import java.util.Date;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.orienteer.core.widget.Widget;
import org.orienteer.oposter.model.IContent;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Calendar widget for page with all content
 */
@Widget(id="cal-all-content", domain="browse", tab = "calendar", selector=IContent.CLASS_NAME, order=10, autoEnable=true)
public class CalendarAllContentWidget extends AbstractCalendarContentWidget<OClass> {
	
	public CalendarAllContentWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
	}

	@Override
	public List<IContent> lookupContent(Date start, Date end) {
		return dao.findContent(start, end);
	}

}

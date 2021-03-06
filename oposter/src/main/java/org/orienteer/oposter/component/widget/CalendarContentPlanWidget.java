package org.orienteer.oposter.component.widget;

import java.util.Date;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.orienteer.core.widget.Widget;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IContentPlan;

import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Calendar window for content within particular content plan
 */
@Widget(id="cal-content-plan", domain="document", tab = "calendar", selector=IContentPlan.CLASS_NAME, order=10, autoEnable=true)
public class CalendarContentPlanWidget extends AbstractCalendarContentWidget<ODocument> {

	public CalendarContentPlanWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
	}

	@Override
	public List<IContent> lookupContent(Date start, Date end) {
		return dao.findContentByContentPlan(getModelObject(), start, end);
	}

}

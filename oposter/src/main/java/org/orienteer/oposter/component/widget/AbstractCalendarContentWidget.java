package org.orienteer.oposter.component.widget;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.Url.StringMode;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.web.ODocumentPage;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.oposter.component.FullCalendar;
import org.orienteer.oposter.component.FullCalendar.Event;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IOPosterDAO;
import org.orienteer.oposter.web.OAuthCallbackResource;
import org.orienteer.vuecket.descriptor.VueFile;
import org.orienteer.vuecket.descriptor.VueJson;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Abstract class for all calendars widgets
 * @param <T> the type of main data object linked to this widget
 */
@VueJson
public abstract class AbstractCalendarContentWidget<T> extends AbstractWidget<T> {
	
	@Inject
	protected IOPosterDAO dao;

	public AbstractCalendarContentWidget(String id, IModel<T> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
		add(new FullCalendar("fullCalendar") {
			@Override
			public List<Event> lookupEvents(Date start, Date end) {
				return AbstractCalendarContentWidget.this.lookupEvents(start, end);
			}
		});
	}

	@Override
	protected FAIcon newIcon(String id) {
		return new FAIcon(id, FAIconType.calendar);
	}

	@Override
	protected IModel<String> getDefaultTitleModel() {
		return new ResourceModel("widget.calendar");
	}
	
	public List<Event> lookupEvents(Date start, Date end) {
		UrlRenderer renderer = RequestCycle.get().getUrlRenderer();
		return lookupContent(start, end).stream().map(c -> {
			Event e = new Event();
			e.setTitle(c.getTitle());
			e.setStart(c.getWhen());
			e.setUrl(renderer.renderRelativeUrl(ODocumentPage.getLinkToTheDocument(DAO.asDocument(c), DisplayMode.VIEW)));
			return e;
		}).collect(Collectors.toList());
	}
	
	public abstract List<IContent> lookupContent(Date start, Date end);

}

package org.orienteer.oposter.component.attachment;

import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.impl.ODocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.orienteer.core.resource.OContentShareResource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Component for displaying attachments
 */
@Slf4j
public class AttachmentsViewPanel extends GenericPanel<OProperty> {

    public static final JavaScriptResourceReference CAROUSEL_LOAD_JS = new JavaScriptResourceReference(AttachmentsViewPanel.class, "carousel-load.js");
    public static final CssResourceReference CAROUSEL_CSS            = new CssResourceReference(AttachmentsViewPanel.class, "carousel.css");

    private final int size;
    private final IModel<ODocument> docModel;

    public AttachmentsViewPanel(String id, int size, IModel<ODocument> docModel, IModel<OProperty> model) {
        super(id, model);
        this.size = size;
        this.docModel = docModel;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(createCarousel("carousel"));
        setOutputMarkupPlaceholderTag(true);
    }

    private WebMarkupContainer createCarousel(String id) {
        return new WebMarkupContainer(id) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                add(createIndicators("carouselIndicators"));
                add(createImagesList("carouselImagesList"));
                add(createButton("prevButton"));
                add(createButton("nextButton"));
                setOutputMarkupPlaceholderTag(true);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(size > 0);
            }
        };
    }

    private ListView<Integer> createIndicators(String id) {
        return new ListView<Integer>(id, new ListModel<>(getIndicators())) {
            @Override
            protected void populateItem(ListItem<Integer> item) {
                if (item.getModelObject() == 0) {
                    item.add(AttributeModifier.append("class", "active"));
                }

                item.add(AttributeModifier.replace("data-slide-to", item.getModelObject()));
                item.add(AttributeModifier.replace("data-target", "#" + getParent().getMarkupId()));
            }

            @Override
            protected void onInitialize() {
                super.onInitialize();
                setReuseItems(true);
            }
        };
    }

    private ListView<Integer> createImagesList(String id) {
        List<Integer> images = IntStream.range(0, size).boxed().collect(Collectors.toList());

        return new ListView<Integer>(id, new ListModel<>(images)) {
            @Override
            protected void populateItem(ListItem<Integer> item) {
                if (item.getIndex() == 0) {
                    item.add(AttributeModifier.append("class", "active"));
                }

                String field = AttachmentsViewPanel.this.getModelObject().getName() + "[" + item.getModelObject() + "].data";

                item.add(new Image("image") {
                    @Override
                    protected String buildSrcAttribute(ComponentTag tag) {
                        return "/" + OContentShareResource.urlFor(docModel.getObject(), field, null, 640,false).toString();
                    }
                });
            }
        };
    }

    private Link<Void> createButton(String id) {
        return new Link<Void>(id) {
            @Override
            public void onClick() {}

            @Override
            protected void onInitialize() {
                super.onInitialize();
                add(AttributeModifier.replace("href", "#" + getParent().getMarkupId()));
            }
        };
    }

    private List<Integer> getIndicators() {
        if (size == 0) {
            return Collections.emptyList();
        }

        return IntStream.range(0, size)
                .boxed()
                .collect(Collectors.toList());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(CAROUSEL_CSS));
        response.render(JavaScriptHeaderItem.forReference(CAROUSEL_LOAD_JS));


        String initScript = String.format("initLoad('%s');", get("carousel").getMarkupId());

        response.render(OnDomReadyHeaderItem.forScript(initScript));
    }
}

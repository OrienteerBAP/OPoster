package org.orienteer.oposter.component.attachment;

import org.apache.tika.Tika;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.oposter.model.IImageAttachment;

public class SingleAttachmentEditPanel extends GenericPanel<IImageAttachment> {

    public SingleAttachmentEditPanel(String id, IModel<IImageAttachment> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        byte[] data = getModelObject().getData();
        add(new Label("name", PropertyModel.of(getModel(), "name")));
        add(new Image("image", new ByteArrayResource(new Tika().detect(data), data)));
        add(createDeleteButton("deleteBtn"));
        setOutputMarkupPlaceholderTag(true);
    }


    private AjaxLink<Void> createDeleteButton(String id) {
        return new AjaxLink<Void>(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                IModel<IImageAttachment> model = SingleAttachmentEditPanel.this.getModel();
                send(
                        SingleAttachmentEditPanel.this.getParent(),
                        Broadcast.BUBBLE,
                        new DeleteAttachmentEventPayload(SingleAttachmentEditPanel.this, DisplayMode.EDIT.asModel(), model, target)
                );
            }
        };
    }


}

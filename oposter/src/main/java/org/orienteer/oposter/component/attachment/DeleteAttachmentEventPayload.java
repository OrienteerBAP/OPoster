package org.orienteer.oposter.component.attachment;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.oposter.component.AbstractEventPayload;
import org.orienteer.oposter.model.IImageAttachment;

/**
 * Payload for events about deletion of some attachment 
 */
public class DeleteAttachmentEventPayload extends AbstractEventPayload {

    private final IModel<IImageAttachment> model;

    public DeleteAttachmentEventPayload(Component component, IModel<DisplayMode> modeModel,
                                        IModel<IImageAttachment> model, AjaxRequestTarget target) {
        super(component, modeModel, target);
        this.model = model;
    }

    public IModel<IImageAttachment> getModel() {
        return model;
    }
}

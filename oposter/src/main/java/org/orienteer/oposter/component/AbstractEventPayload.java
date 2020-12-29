package org.orienteer.oposter.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.orienteer.core.component.property.DisplayMode;

import java.io.Serializable;

public abstract class AbstractEventPayload implements Serializable {

    private final Component component;
    private final IModel<DisplayMode> modeModel;
    private final AjaxRequestTarget target;

    private boolean processed;

    protected AbstractEventPayload(Component component, IModel<DisplayMode> modeModel, AjaxRequestTarget target) {
        this.component = component;
        this.modeModel = modeModel;
        this.target = target;
        this.processed = false;
    }

    protected AbstractEventPayload(Component component, AjaxRequestTarget target) {
        this(component, DisplayMode.VIEW.asModel(), target);
    }

    public Component getComponent() {
        return component;
    }

    public IModel<DisplayMode> getModeModel() {
        return modeModel;
    }

    public AjaxRequestTarget getTarget() {
        return target;
    }

    public boolean isProcessed() {
        return processed;
    }

    public AbstractEventPayload setProcessed(boolean processed) {
        this.processed = processed;
        return this;
    }
}

package org.orienteer.oposter.component.attachment;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.visualizer.AbstractSimpleVisualizer;
import org.orienteer.oposter.model.IImageAttachment;

import java.util.List;

public class AttachmentsVisualizer extends AbstractSimpleVisualizer {

    public static final String NAME = "attachments";

    public AttachmentsVisualizer() {
        super(NAME, false, OType.LINKLIST, OType.LINKSET);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> Component createComponent(String id, DisplayMode mode,
                                         IModel<ODocument> documentModel,
                                         IModel<OProperty> propertyModel, IModel<V> valueModel) {
        if (!propertyModel.getObject().getLinkedClass().isSubClassOf(IImageAttachment.CLASS_NAME)) {
            return null;
        }

        switch (mode) {
            case EDIT:
                return new AttachmentsEditPanel(id, documentModel, (IModel<List<OIdentifiable>>) valueModel);
            case VIEW:

                IModel<List<OIdentifiable>> attachments = (IModel<List<OIdentifiable>>) valueModel;
                int size = attachments.getObject() != null ? attachments.getObject().size() : 0;
                return new AttachmentsViewPanel(id, size, documentModel, propertyModel);
        }
        return null;
    }
}

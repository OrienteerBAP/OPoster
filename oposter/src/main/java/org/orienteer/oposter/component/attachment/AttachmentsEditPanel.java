package org.orienteer.oposter.component.attachment;

import com.google.common.collect.Comparators;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.dao.DAO;
import org.orienteer.oposter.OPUtils;
import org.orienteer.oposter.model.IContent;
import org.orienteer.oposter.model.IImageAttachment;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AttachmentsEditPanel extends FormComponentPanel<List<OIdentifiable>> {

  private static final CssResourceReference EDIT_ATTACHMENT_CSS = new CssResourceReference(AttachmentsEditPanel.class, "edit-attachment.css");
  private static final JavaScriptResourceReference EDIT_ATTACHMENT_JS = new JavaScriptResourceReference(AttachmentsEditPanel.class, "edit-attachment.js");

  private final IModel<ODocument> contentModel;

  private final IModel<List<IImageAttachment>> attachmentsForDelete;
  private WebMarkupContainer uploadWrapper;
  private WebMarkupContainer label;
  private FileUploadField uploadField;

  private boolean submitOnChange = false;
  private boolean immediateDelete = false;
  private boolean uploadOnce = false;

  public AttachmentsEditPanel(String id, IModel<ODocument> contentModel, IModel<List<OIdentifiable>> model) {
    super(id, model);
    this.contentModel = contentModel;
    this.attachmentsForDelete = new ListModel<>(new LinkedList<>());
  }

  @Override
  public void convertInput() {
    List<FileUpload> fileUploads = uploadField.getFileUploads();
    List<IImageAttachment> attachments = new LinkedList<>(getPreparedAttachments());
    if (!fileUploads.isEmpty()) {
      int lastOrder = attachments.isEmpty() ? 0 : attachments.get(attachments.size() - 1).getOrder();

      for (int i = 0; i < fileUploads.size(); i++) {
        FileUpload fu = fileUploads.get(i);
        attachments.add(IImageAttachment.create(DAO.provide(IContent.class, contentModel.getObject()), 
        										fu.getClientFileName(), 
        										getImage(fu), 
        										lastOrder + (i + 1) * 10));
      }
    }
    attachmentsForDelete.getObject().forEach(a -> {
    	DAO.asDocument(a).delete();
    });

    if (!attachments.isEmpty()) {
      setConvertedInput(attachments.stream().map(DAO::asDocument).collect(Collectors.toList()));
    } else {
      setConvertedInput(null);
    }
  }

  @Override
  protected void onInitialize() {
    super.onInitialize();
    uploadWrapper = createUploadWrapper("uploadWrapper");
    uploadWrapper.setOutputMarkupPlaceholderTag(true);
    uploadField = createUploadImagesField("uploadImages");

    label = new WebMarkupContainer("label");
    label.setOutputMarkupPlaceholderTag(true);

    uploadWrapper.add(uploadField);
    uploadWrapper.add(label);

    add(createAttachmentsList("attachments"));
    add(uploadWrapper);


    uploadField.setOutputMarkupId(true);
    setOutputMarkupPlaceholderTag(true);
  }

  @Override
  public void onEvent(IEvent<?> event) {
    super.onEvent(event);

    Object payload = event.getPayload();
    if (payload instanceof DeleteAttachmentEventPayload) {
      DeleteAttachmentEventPayload deletePayload = (DeleteAttachmentEventPayload) payload;

      if (immediateDelete) {
        List<OIdentifiable> attachments = getModelObject();
        if (attachments != null && !attachments.isEmpty()) {
          attachments = new LinkedList<>(attachments);
          attachments.remove(DAO.asDocument(deletePayload.getModel().getObject()));
          setModelObject(attachments);
        }
        deletePayload.getTarget().add(this);
      } else {
        List<IImageAttachment> attachments = attachmentsForDelete.getObject();
        IImageAttachment attachment = deletePayload.getModel().getObject();

        if (attachments.contains(attachment)) {
          attachments.remove(attachment);
        } else {
          attachments.add(attachment);
        }

        deletePayload.getTarget().add(deletePayload.getComponent());
      }

      deletePayload.setProcessed(true);
    }
  }

  @Override
  public void renderHead(IHeaderResponse response) {
    super.renderHead(response);
    response.render(CssHeaderItem.forReference(EDIT_ATTACHMENT_CSS));
    response.render(JavaScriptHeaderItem.forReference(EDIT_ATTACHMENT_JS));

    String initJs = String.format("initEditAttachments('%s', '%s', '%s');",
            uploadWrapper.getMarkupId(), uploadField.getMarkupId(), label.getMarkupId());

    response.render(OnDomReadyHeaderItem.forScript(initJs));
  }

  public void setSubmitOnChange(boolean submitOnChange) {
    this.submitOnChange = submitOnChange;
  }

  public void setImmediateDelete(boolean immediateDelete) {
    this.immediateDelete = immediateDelete;
  }

  public void setUploadOnce(boolean uploadOnce) {
    this.uploadOnce = uploadOnce;
  }

  private List<IImageAttachment> getPreparedAttachments() {
	List<OIdentifiable> attachments =  getModelObject();
	if(attachments==null || attachments.isEmpty()) return Collections.emptyList();
	else return getModelObject()
            .stream()
            .map(d->DAO.provide(IImageAttachment.class, (ODocument)d.getRecord()))
            .filter(a -> !attachmentsForDelete.getObject().contains(a))
            .sorted(Comparator.comparing(IImageAttachment::getOrder, Comparator.nullsFirst(Comparator.naturalOrder())))
            .collect(Collectors.toList());
  }

  private FileUploadField createUploadImagesField(String id) {
    return new FileUploadField(id, new ListModel<>(new LinkedList<>())) {
      @Override
      protected void onInitialize() {
        super.onInitialize();
        setOutputMarkupId(true);
        add(new OImageValidator());
        if (submitOnChange) {
          add(new AjaxFormSubmitBehavior("change") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
              target.add(AttachmentsEditPanel.this);
            }
          });
        }
      }
    };
  }

  private WebMarkupContainer createUploadWrapper(String id) {
    return new WebMarkupContainer(id) {
      @Override
      protected void onConfigure() {
        super.onConfigure();
        if (uploadOnce) {
          setVisible(getModelObject() == null || getModelObject().isEmpty());
        }
      }

      @Override
      protected void onInitialize() {
        super.onInitialize();
        setOutputMarkupPlaceholderTag(true);
      }
    };
  }

  private ListView<IImageAttachment> createAttachmentsList(String id) {
    return new ListView<IImageAttachment>(id, createAttachmentsModel()) {
      @Override
      protected void populateItem(ListItem<IImageAttachment> item) {
        if (item.getIndex() > 0) {
          item.add(AttributeModifier.append("class", "mt-2"));
        }
        item.add(createOfferAttachmentEditPanel("attachment", item.getModel()));
      }

      @Override
      protected void onInitialize() {
        super.onInitialize();
        setReuseItems(true);
      }

      @Override
      protected void onConfigure() {
        super.onConfigure();
        if (immediateDelete) {
          getModel().detach();
        }
      }
    };
  }

  private IModel<List<IImageAttachment>> createAttachmentsModel() {
    return new LoadableDetachableModel<List<IImageAttachment>>() {
      @Override
      protected List<IImageAttachment> load() {
    	 return getPreparedAttachments();
      }
    };
  }

  private SingleAttachmentEditPanel createOfferAttachmentEditPanel(String id, IModel<IImageAttachment> model) {
    return new SingleAttachmentEditPanel(id, model) {
      @Override
      protected void onConfigure() {
        super.onConfigure();
        if (attachmentsForDelete.getObject().contains(getModelObject())) {
          add(AttributeModifier.replace("class", "delete-attachment"));
        } else {
          add(AttributeModifier.replace("class", ""));
        }
      }
    };
  }

  private byte[] getImage(FileUpload fileUpload) {
    byte[] originalImage = fileUpload.getBytes();
    try {
      return OPUtils.resizeImage(fileUpload.getBytes());
    } catch (IOException e) {
      log.error("Can't resize image. Using original image. Original image: name = {}, content type = {}, size = {} b",
              fileUpload.getClientFileName(), fileUpload.getContentType(), originalImage.length, e);
    }
    return originalImage;
  }
}

package org.orienteer.oposter.component.attachment;

import com.google.common.base.Strings;
import org.apache.tika.Tika;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import java.util.Arrays;
import java.util.List;

public class OImageValidator implements IValidator<List<FileUpload>> {

  private static final List<String> SUPPORTED_IMAGES = Arrays.asList("png", "jpg", "jpeg", "bmp", "wbmp", "gif");

  @Override
  public void validate(IValidatable<List<FileUpload>> validatable) {
    Tika tika = new Tika();
    List<FileUpload> uploads = validatable.getValue();

    for (FileUpload upload : uploads) {
      byte[] image = upload.getBytes();
      String type = tika.detect(image);

      if (!isSupportedImageType(type)) {
        ValidationError error = new ValidationError();
        error.setMessage(new ResourceModel("validation.image.upload.not.supported.type").getObject());
        validatable.error(error);
        break;
      }
    }
  }

  private boolean isSupportedImageType(String type) {
    if (Strings.isNullOrEmpty(type) || !type.startsWith("image")) {
      return false;
    }
    String imageType = type.split("/")[1];
    return SUPPORTED_IMAGES.contains(imageType.toLowerCase());
  }

}

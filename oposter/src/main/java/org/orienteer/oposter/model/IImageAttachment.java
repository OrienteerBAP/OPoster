package org.orienteer.oposter.model;

import org.apache.tika.Tika;
import org.apache.wicket.util.string.Strings;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;

import com.google.inject.ProvidedBy;

@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IImageAttachment.CLASS_NAME)
public interface IImageAttachment {
	public static final String CLASS_NAME = "OPImageAttachment";
	
	public String getName();
	public void setName(String value);
	
	public byte[] getData();
	public void setData(byte[] value);
	
	public String getContentType();
	public void setContentType(String value);
	
	public Integer getOrder();
	public void setOrder(Integer value);
	
	@DAOField(inverse = "images")
	public IContent getContent();
	public void setContent(IContent value);
	
	public static IImageAttachment create(IContent content, String name, byte[] data, int order, String contentType) {
		IImageAttachment ret = DAO.create(IImageAttachment.class);
		ret.setContent(content);
		ret.setName(name);
		ret.setData(data);
		ret.setOrder(order);
		ret.setContentType(contentType);
		return ret;
	}
	
	public default String detectContentType() {
		String contentType = getContentType();
		if(Strings.isEmpty(contentType)) {
			byte[] data = getData();
			if(data!=null) contentType = new Tika().detect(getData());
		}
		return contentType;
	}
	
}

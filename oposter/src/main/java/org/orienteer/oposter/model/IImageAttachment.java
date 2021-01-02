package org.orienteer.oposter.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.tika.Tika;
import org.apache.wicket.util.string.Strings;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;

import com.google.common.io.FileBackedOutputStream;
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
	
	@DAOField(hidden = true)
	public String getTempFilePath();
	public void setTempFilePath(String value);
	
	
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
	
	public default File asFile() throws IOException {
		String path = getTempFilePath();
		File file= path!=null?new File(path):null;
		if(file!=null && file.exists()) return file;
		file = Files.createTempDirectory("oposter").resolve(getName()).toFile();
		Files.write(file.toPath(), getData());
		setTempFilePath(file.getAbsolutePath());
		if(DAO.asDocument(this).getIdentity().isPersistent()) DAO.save(this);
		file.deleteOnExit();
		return file;
	}
	
}

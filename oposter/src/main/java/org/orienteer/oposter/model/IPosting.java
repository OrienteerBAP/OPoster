package org.orienteer.oposter.model;

import java.util.Date;

import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOField;
import org.orienteer.core.dao.DAOOClass;
import org.orienteer.core.dao.ODocumentWrapperProvider;

import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * {@link IPosting} is an DAO class for storing of facts of postings to a social networks
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@DAOOClass(value = IPosting.CLASS_NAME,
				displayable = {"content", "channel", "posted", "successful", "url"},
				nameProperty = "content",
				parentProperty = "channel")
public interface IPosting {
	public static final String CLASS_NAME = "OPPosting";
	
	@DAOField(inverse = "postings")
	public IContent getContent();
	public IPosting setContent(IContent value);
	
	@DAOField(inverse = "postings")
	public IChannel getChannel();
	public IPosting setChannel(IChannel value);
	
	@DAOField(type = OType.DATETIME, defaultValue = "sysdate()", readOnly = true)
	public Date getPosted();
	public IPosting setPosted(Date value);

	@DAOField(notNull = true, defaultValue = "true")
	public boolean isSuccessful();
	public IPosting setSuccessful(boolean value);
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_URL_LINK)
	public String getUrl();
	public IPosting setUrl(String value);
	
	public default IPosting setUrl(String format, Object... args) {
		return setUrl(String.format(format, args));
	}
	
	public String getExternalPostingId();
	public IPosting setExternalPostingId(String value);
	
	public default IPosting setExternalPostingId(Object value) {
		if(value!=null) setExternalPostingId(value.toString());
		return this;
	}
	
	@DAOField(visualization = UIVisualizersRegistry.VISUALIZER_TEXTAREA)
	public String getMessage();
	public IPosting setMessage(String value);
	
	public static IPosting createFor(IChannel channel, IContent content) {
		IPosting posting = DAO.create(IPosting.class);
		posting.setChannel(channel);
		posting.setContent(content);
		return posting;
	}
}

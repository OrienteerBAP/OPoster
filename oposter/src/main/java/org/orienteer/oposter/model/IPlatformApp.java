package org.orienteer.oposter.model;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.util.lang.Exceptions;
import org.orienteer.core.OClassDomain;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.ODocumentWrapperProvider;
import org.orienteer.core.dao.OrienteerOClass;
import org.orienteer.core.dao.OrienteerOProperty;
import org.orienteer.logger.OLogger;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.EntityType;

import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Class which holds configuration about connectivity to some social media
 */
@ProvidedBy(ODocumentWrapperProvider.class)
@EntityType(value = IPlatformApp.CLASS_NAME, isAbstract = true)
@OrienteerOClass(domain = OClassDomain.BUSINESS,
           		 displayable = {"name", "description"})
public interface IPlatformApp {
	public static final String CLASS_NAME = "OPPlatformApp";
	
	public String getName();
	public void setName(String name);
	
	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_TEXTAREA)
	public String getDescription();
	public void setDescription(String value);
	
	@EntityProperty(inverse = "platformApp")
	@OrienteerOProperty(visualization = UIVisualizersRegistry.VISUALIZER_TABLE)
	public List<IChannel> getChannels();
	public void setChannels(List<IChannel> value);
	
	public default IPosting send(IChannel channel, IContent content) throws Exception {
		throw new IllegalStateException("Class "+this.getClass().getName()+" should override send(...) method");
	}
	
	public default IPosting sendSafe(IChannel channel, IContent content) {
		IPosting ret;
		try {
			ret = send(channel, content);
		} catch (Throwable e) {
			OLogger.log(e, DAO.asDocument(channel).getIdentity().toString());
			ret = IPosting.createFor(channel, content);
			ret.setSuccessful(false);
			ret.setMessage(ExceptionUtils.getStackTrace(e));
		}
		DAO.save(ret);
		return ret;
	}
	
	public default String getMetadataKey() {
		ODocument doc = DAO.asDocument(this);
		return doc.getSchemaClass().getName()+doc.getIdentity()+doc.getVersion();
	}
	
	public default <M extends IChannel> M checkChannelType(IChannel channel, Class<M> requiredClass){
		if(requiredClass.isInstance(channel)) return (M) channel;
		else throw new IllegalStateException("Incorrect channel class. Expected: "+requiredClass.getName());
	}
	
}

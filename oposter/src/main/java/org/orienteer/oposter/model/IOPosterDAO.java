package org.orienteer.oposter.model;

import java.util.Date;
import java.util.List;

import org.orienteer.core.dao.DAO;
import org.orienteer.core.dao.DAOProvider;
import org.orienteer.transponder.annotation.Query;

import com.google.inject.ProvidedBy;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * DAO for OPoster
 */
@ProvidedBy(DAOProvider.class)
public interface IOPosterDAO {

	@Query("select from "+IContent.CLASS_NAME+" where published!=true and when < sysdate()")
	public List<IContent> findContentToSend();
	
	@Query("select from "+IContent.CLASS_NAME+" where when between :start and :end")
	public List<IContent> findContent(Date start, Date end);
	
	@Query("select from "+IContent.CLASS_NAME+" where contentPlan = :contentPlan and when between :start and :end")
	public List<IContent> findContentByContentPlan(OIdentifiable contentPlan, Date start, Date end);
	
	public default List<IContent> findContentByContentPlan(IContentPlan contentPlan, Date start, Date end) {
		return findContentByContentPlan(DAO.asDocument(contentPlan), start, end);
	}
	
	@Query("select from "+IContent.CLASS_NAME+" where channels contains :channel and when between :start and :end")
	public List<IContent> findContentByChannel(OIdentifiable channel, Date start, Date end);
	
	public default List<IContent> findContentByChannel(IChannel channel, Date start, Date end) {
		return findContentByChannel(DAO.asDocument(channel), start, end);
	}
	
	@Query("select from "+IContent.CLASS_NAME+" where channels.platformApp contains :app and when between :start and :end")
	public List<IContent> findContentByPlatformApp(OIdentifiable app, Date start, Date end);
	
	public default List<IContent> findContentByPlatformApp(IPlatformApp app, Date start, Date end) {
		return findContentByPlatformApp(DAO.asDocument(app), start, end);
	}
}

package org.orienteer.oposter.model;

import java.util.List;

import org.orienteer.core.dao.DAOProvider;
import org.orienteer.core.dao.Query;

import com.google.inject.ProvidedBy;

@ProvidedBy(DAOProvider.class)
public interface IOPosterDAO {

	@Query("select from "+IContent.CLASS_NAME+" where published!=true and when < sysdate()")
	public List<IContent> findContentToSend();
}

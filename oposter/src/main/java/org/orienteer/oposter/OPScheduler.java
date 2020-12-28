package org.orienteer.oposter;

import org.apache.wicket.ThreadContext;
import org.orienteer.core.OrienteerWebApplication;

import com.orientechnologies.orient.core.db.ODatabaseSession;

import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

public class OPScheduler {
	private static OPScheduler INSTANCE;
	
	public static synchronized final OPScheduler getInstance() {
		if(INSTANCE==null) {
			INSTANCE = new OPScheduler();
		}
		return INSTANCE;
	}
	
	public void tick() {
		new DBClosure<Boolean>() {

			@Override
			protected Boolean execute(ODatabaseSession db) {
				ThreadContext.setApplication(OrienteerWebApplication.lookupApplication());
				try {
					tick(db);
					return true;
				} finally {
					ThreadContext.detach();
				}
			}
		}.execute();
	}
	
	protected void tick(ODatabaseSession db) {
		
	}
}

package org.orienteer.oposter.web;

import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.module.PerspectivesModule;
import org.orienteer.oposter.OPosterModule;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.ORule.ResourceGeneric;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static ru.ydn.wicket.wicketorientdb.security.OrientPermission.*;

import java.util.Optional;

/**
 * Module to customize standalone OPoster installation
 */
public class OPosterAppModule extends AbstractOrienteerModule {
	public static final String NAME = "oposter-app";
	

	protected OPosterAppModule() {
		super(NAME, 1, OPosterModule.NAME);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseSession db) {
		reduceReaderRights(db);
		changeDefaultPerspective(db);
		return null;
	}
	
	@Override
	public void onUpdate(OrienteerWebApplication app, ODatabaseSession db, int oldVersion, int newVersion) {
		onInstall(app, db);
	}
	
	protected void reduceReaderRights(ODatabaseSession db) {
		OSecurity security = db.getMetadata().getSecurity();
		ORole readerRole = security.getRole("reader");
		int permissionToRevoke = combinedPermission(CREATE, READ, UPDATE, DELETE, EXECUTE);
		readerRole.revoke(ResourceGeneric.CLASS, null, permissionToRevoke);
		readerRole.revoke(ResourceGeneric.CLUSTER, null, permissionToRevoke);
	}
	
	protected void changeDefaultPerspective(final ODatabaseSession db) {
		final PerspectivesModule perspectivesModule = OrienteerWebApplication.get().getServiceInstance(PerspectivesModule.class);
		perspectivesModule.getPerspectiveByAliasAsDocument(OPosterModule.PERSPECTIVE_ALIAS).ifPresent((p) -> {
			for(ODocument role : db.getMetadata().getSecurity().getAllRoles()) {
				perspectivesModule.updateUserPerspective(role, p);
			}
		});
	}


}

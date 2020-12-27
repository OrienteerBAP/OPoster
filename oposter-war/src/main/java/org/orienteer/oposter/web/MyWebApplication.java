package org.orienteer.oposter.web;

import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.PerspectivesModule;

public class MyWebApplication extends OrienteerWebApplication
{
	@Override
	public void init()
	{
		super.init();
		mountPackage("org.orienteer.oposter.web.web");
		registerModule(DataModel.class);
	}
	
}

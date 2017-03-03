package com.axelor.sales.web;

import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.eventadapter.ImageEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IImageInstance;

public class BIRTImageEventHandler extends ImageEventAdapter {
	
	@Override
	public void onRender( IImageInstance image, IReportContext reportContext )
			throws ScriptException
	{
		image.setData((byte[]) reportContext.getAppContext().get("logo.png"));
	}
}

package org.bladerunnerjs.core.plugin.utilClasses;

import org.bladerunnerjs.core.plugin.AbstractPlugin;
import org.bladerunnerjs.model.BRJS;

public class MyTestInterfaceImplementer extends AbstractPlugin implements MyTestInterface {
	public MyTestInterfaceImplementer() {
	}
	
	@Override
	public void setBRJS(BRJS brjs) {
		// do nothing
	}
}
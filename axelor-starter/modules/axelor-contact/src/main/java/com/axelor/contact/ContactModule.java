package com.axelor.contact;

import com.axelor.app.AxelorModule;
import com.axelor.contact.service.HelloService;
import com.axelor.contact.service.HelloServiceImpl;

public class ContactModule extends AxelorModule {

	@Override
	protected void configure() {
		bind(HelloService.class).to(HelloServiceImpl.class);
	}
}

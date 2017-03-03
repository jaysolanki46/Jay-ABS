package com.axelor.contact.web;

import com.axelor.contact.db.Contact;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ContactController {

	public void hello(ActionRequest req, ActionResponse res) {
		
		Contact c = req.getContext().asType(Contact.class);
		
		res.setValue("lastName", c.getLastName().toUpperCase());
	}
}

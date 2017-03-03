package com.axelor.contact.service;

public class HelloServiceImpl implements HelloService {

	@Override
	public void say(String what) {
		System.err.println("Say: " + what);
	}
}

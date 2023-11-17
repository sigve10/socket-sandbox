package com.example.testclasses;

import com.example.server.ServerConnection;

public class TestLogic  {
	int testValue;

	public TestLogic(ServerConnection server) {
		testValue = 0;	
	}

	public void plusValue(){
		testValue ++;
	}

	public int getTestValue() {
		return testValue;
	}
}

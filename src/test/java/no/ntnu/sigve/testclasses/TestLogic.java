package no.ntnu.sigve.testclasses;

import no.ntnu.sigve.server.ServerConnection;

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

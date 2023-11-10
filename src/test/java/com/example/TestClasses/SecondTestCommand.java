package com.example.TestClasses;

import com.example.server.commands.Command;
import com.example.server.handler.Logic;

/**
 * A command class for use in test cases.
 */

public class SecondTestCommand extends Command{

	public SecondTestCommand() {
		super(new TestLogic());
	}

	@Override
	public String execute(String[] args) {
		return "test2";
	}
	
}

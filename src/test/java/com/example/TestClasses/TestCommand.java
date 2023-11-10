package com.example.TestClasses;

import com.example.server.commands.Command;
import com.example.server.handler.Logic;
/**
 * A command class for use in test cases.
 */

public class TestCommand extends Command {

	/**
	 * homie.
	 */
	public TestCommand() {
		super(new TestLogic());
	}

	protected TestCommand(Logic actor) {
		super(actor);
		//TODO Auto-generated constructor stub
	}

	@Override
	public String execute(String[] args) {
		return "test1";
	}
	
}

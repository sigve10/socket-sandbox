package com.example.server.commands;

import com.example.handler.Logic;

/**
 * Abstract class responsible for executing commands.
 */
public abstract class Command {
	protected int argumentCount;
	protected String[] args;
	
	/**
	 * Creates a command with a fixed number of arguments
	 * @param argumentCount the number of arguments
	 */
	protected Command(int argumentCount) {
		this.argumentCount = argumentCount;
		this.args = new String[argumentCount];
	}

	/**
	 * Executes this commmand.
	 *
	 * @param actor an object on which to perform the command on (can be null)
	 * @param args an array of arguments to run the command with
	 * @return a string response from the command
	 */
	public abstract String execute(Logic actor, String[] args);
}

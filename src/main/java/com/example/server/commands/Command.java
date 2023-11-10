package com.example.server.commands;

import com.example.server.handler.Logic;

/**
 * Abstract class responsible for executing commands.
 *
 * @author Sigve Bjørkedal
 */
public abstract class Command {
	protected final Logic actor;
	
	/**
	 * Creates a command.
	 *
	 * @param actor The actor on which the command should be able to act
	 */
	protected Command(Logic actor) {
		this.actor = actor;
	}

	/**
	 * Executes this commmand.
	 *
	 * @param args an array of arguments to run the command with, where 0 is always the raw command.
	 * @return a string response from the command
	 */
	public abstract String execute(String[] args);
}

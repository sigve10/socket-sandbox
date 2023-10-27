package com.example.server.commands;

public abstract class Command {
	protected int argumentCount;
	protected String[] args;
	
	protected Command(int argumentCount) {
		this.argumentCount = argumentCount;
		this.args = new String[argumentCount];
	}

	public abstract String execute(String[] args);
}

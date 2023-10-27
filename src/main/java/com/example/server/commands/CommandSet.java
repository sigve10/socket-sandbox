package com.example.server.commands;

import java.util.HashMap;
import java.util.Iterator;

import com.example.handler.Logic;

public class CommandSet {
	public static final char COMMAND_SEPARATOR = ' ';
	private HashMap<String, Command> commands;

	private CommandSet(CommandSetBuilder builder) {
		this.commands = builder.commands;
	}

	public String tryToExecuteCommand(Logic actor, String rawCommand) {
		String[] commandContents = rawCommand.split(String.valueOf(COMMAND_SEPARATOR));
		String commandWord = commandContents[0];
		Iterator<String> iterator = commands.keySet().iterator();

		Command command = null;
		String result = null;

		while (iterator.hasNext() && command == null) {
			String current = iterator.next();
			
			if (commandWord.matches(current)) {
				command = commands.get(current);
				result = command.execute(actor, commandContents);
			}
		}

		return result;
	}

	public class CommandSetBuilder {
		private HashMap<String, Command> commands;

		public CommandSetBuilder add(String commandRegex, Command command) {
			this.commands.put(commandRegex, command);
			return this;
		}

		public CommandSet build() {
			return new CommandSet(this);
		}
	}
}

package com.example.server.commands;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A command interpreter class that contains a set of commands that can be executed by a string.
 * Must be built by a {@link CommandSetBuilder}
 *
 * @author Sigve Bjørkedal
 */
public class CommandSet {
	public static final char COMMAND_SEPARATOR = ' ';
	private HashMap<String, Command> commands;

	/**
	 * Creates a command set from a {@link CommandSetBuilder}.
	 *
	 * @param builder the builder to create the command set from
	 */
	private CommandSet(CommandSetBuilder builder) {
		this.commands = builder.commands;
	}

	/**
	 * Attempts to execute a command from the command set given a string. Strings may contain
	 * arguments separated by the {@link CommandSet#COMMAND_SEPARATOR constant command separator}.
	 *
	 * @param rawCommand a string with the raw command message
	 * @return either the result of a command or an invalid command response string
	 */
	public String tryToExecuteCommand(String rawCommand) {
		String[] commandContents = rawCommand.split(String.valueOf(COMMAND_SEPARATOR));
		String commandWord = commandContents[0];
		Iterator<String> iterator = commands.keySet().iterator();

		Command command = null;
		String result = "Invalid command";

		while (iterator.hasNext() && command == null) {
			String current = iterator.next();
			
			if (commandWord.matches(current)) {
				command = commands.get(current);
				if (command != null) {
					result = command.execute(commandContents);
				}
			}
		}

		return result;
	}

	/**
	 * A builder for {@link CommandSet}.
	 */
	public static class CommandSetBuilder {
		private HashMap<String, Command> commands;

		/**
		 * Adds a new command to the command set.
		 *
		 * @param commandRegex a string representing the command. Can be regex.
		 * @param command A {@link Command} to be executed if the command is called
		 * @return this builder in order to chain command
		 */
		public CommandSetBuilder add(String commandRegex, Command command) {
			this.commands.put(commandRegex, command);
			
			return this;
		}

		/**
		 * Creates a {@link CommandSet} from this builder.
		 *
		 * @return the built {@link CommandSet}
		 */
		public CommandSet build() {
			return new CommandSet(this);
		}
	}
}

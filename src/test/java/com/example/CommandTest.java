package com.example;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.example.server.Server;
import com.example.server.commands.Command;
import com.example.server.handler.Logic;


public class CommandTest {
	private Thread serverThread;

	@Before
	public void initializeServerAndClient() {
		serverThread = new Thread(
			() -> {
				try {
					new Server(8080).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		);
		
	}

	@Test
	public void test(){
		
		
		assertTrue(true);
	}


	private class InnerCommandTest extends Command {

		protected InnerCommandTest(Logic actor) {
			super(actor);
		}

		@Override
		public String execute(String[] args) {
			
			return "";
		}
		
		
	}
}

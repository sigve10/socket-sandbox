package com.example.server;

import com.example.server.handler.Logic;

public class Television implements Logic {
	private final int channels;
	private int currentChannel;

	public Television(int channels) {
		this.channels = channels;
	}

	public String increaseChannel() {
		return "1";
	}

	public String decreaseChannel() {
		return "2";
	}

	public String powerOff() {
		return "3";
	}

	public String powerOn() {
		return "4";
	}
}

package com.google.code.laserswarm.simulation;

public class Simulator implements Runnable {

	private SimTemplate	template;
	private Thread		thread;

	public Simulator(SimTemplate templ) {
		this.template = templ;
	}

	public Thread getThread() {
		return thread;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// 
		throw new UnsupportedOperationException();
	}

	public Thread start() {
		thread = new Thread(this, "Simulator");
		thread.start();
		return thread;
	}

}

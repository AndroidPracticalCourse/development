import coppelia.remoteApi;

public class ThreadSupervisor implements Runnable{
	private remoteApi vrep;
	private int clientID;
	private Server server;
	private Thread serverthread;
	public ThreadSupervisor(remoteApi vrep, int clientID){
		this.vrep=vrep;
		this.clientID=clientID;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
			System.out.println("Thread supervisor has started.");
				server = new Server(vrep, clientID);
		    	serverthread = new Thread(server);
		    	serverthread.start();
		    	System.out.println("Started server thread for the first time.");
		 while(true){	
			 if(!serverthread.isAlive()){
				 server = new Server(vrep, clientID);
			    	serverthread = new Thread(server);
			    	serverthread.start();
			    	System.out.println("Server thread is restarted.");
			 }
			 try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				

		}
	}
}


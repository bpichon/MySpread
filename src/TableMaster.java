import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class TableMaster extends UnicastRemoteObject implements Runnable, ITableMaster {

	/**
	 * Serial-ID
	 */
	private static final long serialVersionUID = 2936771239204420876L;
	
	private static final int interval = 1000;

	private ClientStats localStats;
	private Thread thread;
	private IClient client;

	public TableMaster(IClient client) throws RemoteException {
		localStats = new ClientStats(client);
		thread = new Thread(this); 
		this.client = client;
	}
	
	private ClientStats collectStats() throws RemoteException {
		synchronized (localStats) {
			localStats.clear();
			for (IPhilosopher philosopher : client.getPhilosophers()) {
				localStats.add(philosopher, philosopher.getEatingCounter(), philosopher.getEatingTimeFactor());
			}
			localStats.seatCount = client.getSeats().size();
			return localStats;
		}
	}
	
	@Override
	public IClientStats getStats() {
		synchronized (localStats) {
			return localStats;
		}
	}
	
	@Override
	public void run() {
		while(!thread.isInterrupted()) {
			try {
				thread.sleep(interval);
				collectStats();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public void start() {
		thread.start();
	}
}
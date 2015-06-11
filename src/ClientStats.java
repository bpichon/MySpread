import java.rmi.RemoteException;
import java.util.ArrayList;

public class ClientStats implements IClientStats {
	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = -6911058560658781410L;
	
	IClient client;
	int seatCount;
	ArrayList<IPhilosopher> philosophers = new ArrayList<>();
	ArrayList<Integer> eatingCounts = new ArrayList<>();
	ArrayList<Integer> eatingTimeFactor = new ArrayList<>();
	public ClientStats(IClient client) {
		this.client = client;
	}
	
	@Override
	public IClient getClient() throws RemoteException {
		return client;
	}
	
	@Override
	public int getSeatCount() throws RemoteException {
		return seatCount;
	}
	
	@Override
	public ArrayList<Integer> getEatingCount() throws RemoteException {
		return eatingCounts;
	}
	
	@Override
	public ArrayList<Integer> getEatingTimeFactor() throws RemoteException {
		return eatingTimeFactor;
	}
	
	@Override
	public ArrayList<IPhilosopher> getPhilosophers() throws RemoteException {
		return philosophers;
	}
	
	public void add(IPhilosopher philospher, Integer count, Integer meditatingTime) {
		philosophers.add(philospher);
		eatingCounts.add(count);
		eatingTimeFactor.add(meditatingTime);
	}
	
	public void clear() {
		philosophers.clear();
		eatingCounts.clear();
		eatingTimeFactor.clear();
	}
}
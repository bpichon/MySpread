import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IClient extends Remote {

	public int getId() throws RemoteException;

	public void start() throws RemoteException;

	enum Direction {
		LEFT, RIGHT
	}

	public ArrayList<ISeat> getSeats() throws RemoteException;

	public ArrayList<IClient> getAllClients() throws RemoteException;

	public void pause() throws RemoteException;

	public void resume() throws RemoteException;

	void addSuspendedPhilosopher(IPhilosopher philosopher) throws InterruptedException, RemoteException;

	String toMyString() throws RemoteException;

	public ISeat getSeat(int i) throws RemoteException;

	void updateClients() throws RemoteException;

	void addClient(IClient client) throws RemoteException;

	void clearClients() throws RemoteException;

	public ArrayList<IPhilosopher> getPhilosophers() throws RemoteException;

	public ITableMaster getTableMaster() throws RemoteException;

	public void lock(IPhilosopher currentPhilosopher) throws RemoteException;

	void addSeat() throws RemoteException;

	void addPhilospher(int eatCount, int eatingTimeFactor)
			throws RemoteException;

	void reportRemoteException(Exception e) throws RemoteException;
}

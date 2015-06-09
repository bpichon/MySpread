import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IClient extends Remote {

	public void updateClients(ArrayList<IClient> clients)
			throws RemoteException;

	public int getId() throws RemoteException;

	public void start() throws RemoteException;

	// public void setTimer(int eatingTimer, int mediatingTimer, int sleepTimer, int lockTimer)
	// throws RemoteException;

	enum Direction {
		LEFT, RIGHT
	}

	public ArrayList<ISeat> getSeats() throws RemoteException;

	public ArrayList<IClient> getAllClients() throws RemoteException;
}

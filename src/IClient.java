import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IClient extends Remote, Serializable {

	public void updateClients(ArrayList<IClient> clients)
			throws RemoteException;

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
}

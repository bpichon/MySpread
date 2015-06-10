import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPhilosopher extends Remote {

	public IClient getClient() throws RemoteException;

	public void start() throws RemoteException;

	public void setSuspended(boolean isSuspended) throws RemoteException;

	public boolean isAlive() throws RemoteException;

	public int getMyId() throws RemoteException;

	String toMyString() throws RemoteException;

	public Integer getEatingCounter() throws RemoteException;

	public Integer getEatingTimeFactor() throws RemoteException;

	public void lock() throws RemoteException;

	boolean isLocked() throws RemoteException;

}

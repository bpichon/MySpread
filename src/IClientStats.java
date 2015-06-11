import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface IClientStats extends Remote, Serializable {

	IClient getClient() throws RemoteException;

	int getSeatCount() throws RemoteException;

	ArrayList<Integer> getEatingCount() throws RemoteException;

	ArrayList<Integer> getEatingTimeFactor() throws RemoteException;

	ArrayList<IPhilosopher> getPhilosophers() throws RemoteException;

}

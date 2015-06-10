import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISeat extends Remote {

	int tryToSitDown(IPhilosopher philosopher) throws RemoteException;

	boolean sitOrWait(IPhilosopher philosopher) throws RemoteException;;

	boolean standUp(IPhilosopher philosopher) throws RemoteException;

	IPhilosopher getPhilosopher() throws RemoteException;

	IClient getClient() throws RemoteException;

	int getId() throws RemoteException;

	String toMyString() throws RemoteException;

}

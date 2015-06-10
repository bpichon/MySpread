import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISeat extends Remote, Serializable {

	int tryToSitDown(IPhilosopher philosopher) throws RemoteException;

	void sitOrWait(IPhilosopher philosopher) throws RemoteException;;

	void standUp(IPhilosopher philosopher) throws RemoteException;

	IPhilosopher getPhilosopher() throws RemoteException;

	IClient getClient() throws RemoteException;

	int getId() throws RemoteException;

	String toMyString() throws RemoteException;

}

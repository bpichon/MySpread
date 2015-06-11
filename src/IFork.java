import java.rmi.Remote;
import java.rmi.RemoteException;


public interface IFork extends Remote {

	boolean release(ISeat seat) throws RemoteException;

	boolean tryTake(ISeat seat) throws RemoteException;
	
	IClient getClient() throws RemoteException;
	
	int getId() throws RemoteException;

	String toMyString() throws RemoteException;

	//boolean tryTakeTimeout(ISeat seat) throws RemoteException;

}

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface IServer extends Remote {

	/**
	 * Registriert den Client im Server. 
	 * @param client zu registrierender Client
	 * @return false, wenn bereits registriert.
	 */
	public boolean registerClient(IClient client) throws RemoteException;

	void recovery() throws RemoteException; 
	
}

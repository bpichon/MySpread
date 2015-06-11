import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ITableMaster extends Remote {

	IClientStats getStats() throws RemoteException ;

}

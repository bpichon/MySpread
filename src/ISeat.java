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

	void takeBothForks() throws RemoteException;

	boolean releaseBothForks() throws RemoteException;

	IFork getLeftFork() throws RemoteException;

	void setRightFork(IFork fork) throws RemoteException;

	IFork getRightFork() throws RemoteException;


}

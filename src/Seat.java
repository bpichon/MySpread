import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Seat extends UnicastRemoteObject implements ISeat {

	/**
	 * Serial-Id 
	 */
	private static final long serialVersionUID = 6574990956188001831L;
	
	private final IClient client;
	private final int id;
	private final ReentrantLock lock;

	private IPhilosopher philosopher; 
	
	private ArrayList<IPhilosopher> queue;
	
	public Seat(IClient client, int id) throws RemoteException {
		this.client = client;
		this.id = id;
		lock = new ReentrantLock();
		philosopher = null;
		queue = new ArrayList<>();
	}
	
	/**
	 * Nimmt den Platz, wenn er frei ist. Wenn nicht, gibt er die Anzahl der wartenden Philosophen zurück.
	 * @return -1, wenn Platz frei und genommen. ansonsten die Anzahl der Philosophen in der WaitingQueue.
	 */
	@Override
	public int tryToSitDown(IPhilosopher philosopher) throws RemoteException {
		if (lock.tryLock()) {
			if (this.philosopher != null) {
				// FIXME: remove Exception
				//throw new RuntimeException("kann eigentlich nicht sein!##1 - " + this.philosopher.toMyString() + " - " + this);
				System.out.println("kann eigentlich nicht sein!##1 - " + this.philosopher.toMyString() + " - " + this);
			}
			this.philosopher = philosopher;
			System.out.println(philosopher.toMyString() + " hat sich gerade hingesetzt. (direkt) | " + this);
			return -1;
		}
		return queue.size();
	}
	
	@Override
	public void sitOrWait(IPhilosopher philosopher) throws RemoteException {
		queue.add(philosopher);
		System.out.println(philosopher.toMyString() + " zur  Warteschlange hinzugefügt | " + this);
		lock.lock();
		if (this.philosopher != null) {
			// FIXME: remove Exception
			throw new RuntimeException("kann eigentlich nicht sein!");
		}
		System.out.println(philosopher.toMyString() + " hat sich gerade hingesetzt. (über Warteschlange) | " + this);
		this.philosopher = philosopher;
		queue.remove(philosopher);
	}
	
	@Override
	public void standUp(IPhilosopher philosopher) throws RemoteException {
		synchronized (lock) {
				
			IPhilosopher temp = this.philosopher;
			if (philosopher == null) {
				System.out.println("NULLLLLLL");
			}
			if (!lock.isHeldByCurrentThread() || philosopher == null || this.philosopher == null) {return;}
	
			this.philosopher = null;
			System.out.println(philosopher.toMyString() + " ist gerade aufgestanden. | " + this);
			try {
				System.out.println("DEBUG! " + temp.toMyString() + " | " + philosopher.toMyString() + "  ||  " + this + " ||| " + Thread.currentThread());
				lock.unlock();
			} catch (IllegalMonitorStateException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	@Override
	public String toString() {
		int clientId;
		try {
			clientId = client.getId();
		} catch (RemoteException e) {
			clientId = -1;
		}
		
		return "Se("+id+")["+clientId+"]";
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof ISeat) {
			try {
				final ISeat otherSeat = ((ISeat) other);
				return getClient().equals(otherSeat.getClient()) 
						&& getId() == otherSeat.getId();
			} catch (RemoteException e) {
				return false;
			}
		}
		return false;
	}

	@Override
	public IClient getClient() throws RemoteException {
		return client;
	}
	
	@Override
	public IPhilosopher getPhilosopher() throws RemoteException {
		// FIXME: remove me
		return philosopher;
	}

	@Override
	public int getId() throws RemoteException {
		return id;
	}
	
	@Override
	public String toMyString() throws RemoteException {
		return toString();
	}
}

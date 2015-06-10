import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Seat extends UnicastRemoteObject implements ISeat {

	/**
	 * Serial-Id 
	 */
	private static final long serialVersionUID = 6574990956188001831L;
	
	private final IClient client;
	private final int id;
	private final Semaphore lock;

	ArrayList<Object> queue;
	private IPhilosopher philosopher; 
	
	public Seat(IClient client, int id) throws RemoteException {
		super();
		this.client = client;
		this.id = id;
		lock = new Semaphore(1);
		philosopher = null;
		queue = new ArrayList<>();
	}
	
	/**
	 * Nimmt den Platz, wenn er frei ist. Wenn nicht, gibt er die Anzahl der wartenden Philosophen zurück.
	 * @return -1, wenn Platz frei und genommen. ansonsten die Anzahl der Philosophen in der WaitingQueue.
	 */
	@Override
	public int tryToSitDown(IPhilosopher philosopher) throws RemoteException {
		synchronized (lock) {
			if (lock.tryAcquire()) {
				assert this.philosopher == null : "Philosopher dieses Platzes muss 'null' sein, da er gerade neu besetzt wurde.";
				this.philosopher = philosopher;
				System.out.println(philosopher.toMyString() + " hat sich gerade hingesetzt. (direkt) | " + this);
				return -1;
			}
			return queue.size();
		}
	}
	
	@Override
	public boolean sitOrWait(IPhilosopher philosopher) throws RemoteException {
		System.out.println(philosopher.toMyString() + " zur  Warteschlange hinzugefügt | " + this);
		synchronized (lock) {
			queue.add(new Object());
		}
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		synchronized (lock) {
			assert this.philosopher == null : "Philosopher dieses Platzes muss 'null' sein, da er gerade neu besetzt wurde.";
			System.out.println(philosopher.toMyString() + " hat sich gerade hingesetzt. (über Warteschlange) | " + this);
			this.philosopher = philosopher;
			queue.remove(0);
			return true;
		}
	}
	
	@Override
	public boolean standUp(IPhilosopher philosopher) throws RemoteException {
		synchronized (lock) {
			this.philosopher = null;
			System.out.println(philosopher.toMyString() + " ist gerade aufgestanden. | " + this);
			
			lock.release();
			
		}
		assert !Thread.holdsLock(lock) : "Thread darf nicht mehr owner des Locks sein.";
		return true;
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

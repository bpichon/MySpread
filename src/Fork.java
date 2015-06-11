import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Semaphore;


public class Fork extends UnicastRemoteObject implements IFork {
	
	/**
	 * Serial-Id
	 */
	private static final long serialVersionUID = -5449152050028093584L;
	
	private IClient client;
	private int id;
	
	private Semaphore lock;
	private ISeat seat;
	
	public Fork(IClient client, int id) throws RemoteException {
		super();
		this.client = client;
		this.id = id;
		seat = null;
		lock = new Semaphore(1);
	}
	
	/**
	 * Besetzt die Gabel wenn frei, ansonsten gibt sie false zurück.
	 * @param seat Der Sitz, für die die Gabel besetzt wird.
	 * @return true, wenn die Gabel besetzt werden konnte - false ansonsten.
	 */
	@Override
	public boolean tryTake(ISeat seat) throws RemoteException {
		synchronized (lock) {
			if (lock.tryAcquire()) {
				this.seat = seat;
				System.out.println(seat.toMyString() + " hat Fork reserviert: " + this.toString());
				return true;
			}
			return false;
		}
	}
	
	/*
	@Override
	public boolean tryTakeTimeout(ISeat seat) throws RemoteException {
		boolean success = false;
		synchronized (lock) {
			try {
				success = lock.tryAcquire(1000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
			if (success) {
				this.seat = seat;
				return true;
			}
		}
		return false; 
	}*/
	

	/**
	 * Gibt die Gabel wieder frei, wenn der Seat auch der besetzende ist.
	 * @param seat der besetzende Sitz
	 * @return true, wenn freigegeben wurde, false ansonsten
	 */
	@Override
	public boolean release(ISeat seat) throws RemoteException {
		synchronized (lock) {
		if (this.seat != null && this.seat.equals(seat)) {
				lock.release();
				System.out.println(seat.toMyString() + " hat Fork freigegeben: " + this.toString());
				assert lock.availablePermits() == 1 : "Lock ist wieder frei.";
			this.seat = null;
			return true;
		}
		return false;
		}
	}
	
	@Override
	public String toString() {
		int clientId;
		try {
			clientId = client.getId();
		} catch (RemoteException e) {
			clientId = -1;
			try {
				client.reportRemoteException(e);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
		
		return "Fo("+id+")["+clientId+"]";
	}
	
	@Override
	public String toMyString() throws RemoteException {
		return toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof IFork) {
			try {
				final IFork otherSeat = ((IFork) other);
				return getClient().equals(otherSeat.getClient()) 
						&& getId() == otherSeat.getId();
			} catch (RemoteException e) {
				try {
					client.reportRemoteException(e);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
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
	public int getId() throws RemoteException {
		return id;
	}
}

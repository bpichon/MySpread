import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Philosopher extends UnicastRemoteObject implements IPhilosopher, Runnable {

	/**
	 * Serial-Id
	 */
	private static final long serialVersionUID = 2139005198749593791L;

	public static final int sleepingTime = 10;

	public final int lockingTime = 10_000;
	public int meditatingTime = 100;
	public int eatingTime = 10;
	
	private Thread thread;
	
	private final int id;
	private State state;
	private boolean isSuspended = false;
	private boolean isLocked = false;
	

	private final IClient client;
	private ISeat seat;
	private int eatCounter = 0;

	private boolean cancelled = false;

	public Philosopher(IClient client, int id) throws RemoteException {
		this(client, id, 0);
	}
	
	public Philosopher(IClient client, int id, int hunger) throws RemoteException {
		this(client, id, hunger, 0);
	}
	
	public Philosopher(IClient client, int id, int hunger, int eatCounter) throws RemoteException {
		super();
		this.client = client;
		this.id = id;
		this.eatCounter = eatCounter;
		eatingTime = eatingTime + hunger;
		meditatingTime = (meditatingTime - hunger <= 0) ? 1 : meditatingTime - hunger;
		this.seat = null;
		state = State.SEARCHING;
		thread = new Thread(this);
	}

	@Override
	public void run() {
		while (!cancelled) {
			assert seat == null : "Zu dieser Zeit darf der Philosoph keinen Seat zugewiesen haben.";
			State nextState = null;
			
			if (isLocked) {
				// Client bzw Server haben diesen Philosophen gelockt.
				state = State.LOCKED;
			}
			
			if (state == State.SLEEPING) {
				try {
					thread.sleep(sleepingTime);
				} catch (InterruptedException e) {e.printStackTrace();}
				nextState = State.SEARCHING;
				
			} else if (state == State.MEDITATING) {
				try {
					thread.sleep(meditatingTime);
				} catch (InterruptedException e) {e.printStackTrace();}
				nextState = State.SEARCHING;
				
			} else if (state == State.LOCKED) {
				try {
					thread.sleep(lockingTime);
				} catch (InterruptedException e) {e.printStackTrace();}
				isLocked = false;
				nextState = State.SEARCHING; // TODO: vllt auch waiting.
				
			} else {
				// nextState wird in der Methode (standUp) angegeben
				try {
					findSeatAndSit();
				} catch (RemoteException e) {e.printStackTrace();}
				// Der Philosoph kann nun zu essen anfangen.
				eatAndStandUp();
			}
			if (nextState != null) {
				state = nextState;
			} 
		}
	}


	private void findSeatAndSit() throws RemoteException {
		/* Einmal lokal durchlaufen und nach freien Plätzen suchen. Gleichzeitig die kürzeste Schlange suchen. */ 
		ISeat localShortestSeat = null;
		int localShortestQueue = Integer.MAX_VALUE;
		for (ISeat localSeat : client.getSeats()) {
			final int currentLength = localSeat.tryToSitDown(this);
			if (currentLength < 0) {
				// der Philosoph hat sich hingestetzt.
				seat = localSeat;
				assert localSeat.getPhilosopher().equals(this) : "Philosoph wurde soeben zugewiesen, also muss er sich auch auf dem Stuhl befinden";
				return;
			} else if (currentLength < localShortestQueue) {
				localShortestSeat = localSeat;
				localShortestQueue = currentLength;
			}
		}
		
		/* Einmal remote durch alle Clients und Sitze nach freien Plätzen suchen. */
		for (int c = 0; c < client.getAllClients().size(); c++)
		{
			final IClient remoteClient = client.getAllClients().get(c);
			if (remoteClient.equals(client)) continue;
			for (int i = 0; i < remoteClient.getSeats().size(); i++) {
				final ISeat remoteSeat = remoteClient.getSeat(i);
				final int currentLength = remoteSeat.tryToSitDown(this);
				if (currentLength < 0) {
					// der Philosoph hat sich remote hingestetzt.
					seat = remoteSeat;
					assert this.equals(remoteSeat.getPhilosopher()) : "Dieser Sitz ist durch diesen Philosophen besetzt!";
					return;
				}
			}
		}
		
		assert seat == null : "Seat des Philosophen muss nach erfolgloser erster Suche null sein.";
		assert localShortestSeat != null : "LocalShortestSeat kann nicht null sein.";
		
		/* An lokalen Sitz mit kürzester Schlange einreihen. */
		localShortestSeat.sitOrWait(this); // kehrt erst zurück, wenn er tatsächlich am Platz SITZT
		
		assert localShortestSeat.getPhilosopher() != null : "Nachdem der Sitz besetzt wurde, kann der Philosoph auf dem Stuhl nicht 'null' sein!";
		assert this.equals(localShortestSeat.getPhilosopher()) : "Nachdem der Sitz besetzt wurde, muss der Philosoph auf dem Stuhl dieser sein!";
		
		seat = localShortestSeat;
		return; // Der Philosoph sitzt zu diesem Zeitpunkt garantiert an einem Sitz.
	}

	private void eatAndStandUp() {
		final State nextState;
		checkSuspend();
		do {
			try {
				seat.takeBothForks(); // Nachdem suspended wurde Gabeln wieder neu holen
			} catch (RemoteException | NullPointerException e) {
				// Wenn Seat null ist, war es ein entfernter Seat, der wegen Clientabsturz weggebrochen ist. 
				e.printStackTrace();
			}
		} while(checkSuspend());
		try {
			thread.sleep(eatingTime); // isst
			try {
				System.out.println(this + " hat gerade gegessen. Auf Stuhl: " + seat.toMyString());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (++eatCounter % 3 != 0) {
			nextState = State.MEDITATING;
		} else {
			nextState = State.SLEEPING;
		}
		// Fertig gegessen, Stuhl kann wieder freigegeben werden.
		try {
			seat.standUp(this);
		} catch (RemoteException e) {e.printStackTrace();}
		seat = null;
		state = nextState;
	}
	
	private boolean checkSuspend() {
		if (isSuspended) {
			try {
				seat.releaseBothForks();
				client.addSuspendedPhilosopher(this);
			} catch (RemoteException | InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	@Override
	public IClient getClient() throws RemoteException {
		return client;
	}

	@Override
	public int getMyId() throws RemoteException {
		return id;
	}

	@Override
	public String toString() {
		int clientId;
		try {
			clientId = client.getId();
		} catch (RemoteException e) {
			clientId = -1;
		}
		
		return "Ph("+id+")["+clientId+"]";
	}
	
	@Override
	public String toMyString() throws RemoteException {
		return toString();
	}
	
	static enum State {
		MEDITATING, SLEEPING, SEARCHING, /*WAITING,*/SITTING, LOCKED
	}


	@Override
	public void setSuspended(boolean isSuspended) throws RemoteException {
		this.isSuspended = isSuspended;		
	}

	
	@Override
	public boolean equals(Object other) {
		if (other instanceof IPhilosopher) {
			try {
				final IPhilosopher otherPhilosopher = ((IPhilosopher) other);
				return getClient().equals(otherPhilosopher.getClient()) 
						&& getMyId() == otherPhilosopher.getMyId();
			} catch (RemoteException e) {
				return false;
			}
		}
		return false;
	}

	@Override
	public void start() throws RemoteException {
		thread.start();
	}

	@Override
	public boolean isAlive() throws RemoteException {
		return thread.isAlive();
	}

	@Override
	public Integer getEatingCounter() throws RemoteException {
		return eatCounter;
	}

	@Override
	public Integer getEatingTimeFactor() throws RemoteException {
		// TODO return correct value
		return 1;
	}

	@Override
	public void lock() throws RemoteException {
		isLocked = true;
	}
	
	@Override
	public boolean isLocked() throws RemoteException {
		return isLocked;
	}

	@Override
	public Thread.State getState() throws RemoteException {
		return thread.getState(); // TODO: oder nur getState?
	}

}

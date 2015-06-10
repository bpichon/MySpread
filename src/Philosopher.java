import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Philosopher extends Thread implements IPhilosopher {

	/**
	 * Serial-Id
	 */
	private static final long serialVersionUID = 2139005198749593791L;

	public static final int sleepingTime = 10;

	public final int lockingTime = 10_000;
	public final int meditatingTime = 1000;
	public final int eatingTime = 1000;
	
	private final int id;
	private State state;
	private boolean isSuspended = false;
	

	private final IClient client;
	private ISeat seat;
	private int eatCounter = 0;

	private boolean cancelled = false;

	public Philosopher(IClient client, int id) throws RemoteException {
		this.client = client;
		this.id = id;
		this.seat = null;
		state = State.SEARCHING;
		UnicastRemoteObject.exportObject(this, 0);
	}

	@Override
	public void run() {
		while (!cancelled) {
			if (seat != null) {
				// FIXME: remove me
				throw new RuntimeException("WARUM??");
			}
			State nextState = null;
			if (state == State.SLEEPING) {
				try {
					sleep(sleepingTime);
				} catch (InterruptedException e) {e.printStackTrace();}
				nextState = State.SEARCHING;
				
			} else if (state == State.MEDITATING) {
				try {
					sleep(meditatingTime);
				} catch (InterruptedException e) {e.printStackTrace();}
				nextState = State.SEARCHING;
				
			} else if (state == State.LOCKED) {
				try {
					sleep(lockingTime);
				} catch (InterruptedException e) {e.printStackTrace();}
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
		/* Einmal lokal durchlaufen und nach freien Pl�tzen suchen. Gleichzeitig die k�rzeste Schlange suchen. */ 
		ISeat localShortestSeat = null;
		int localShortestQueue = Integer.MAX_VALUE;
		for (ISeat localSeat : client.getSeats()) {
			final int currentLength = localSeat.tryToSitDown(this);
			if (currentLength < 0) {
				// der Philosoph hat sich hingestetzt.
				seat = localSeat;
				if (!localSeat.getPhilosopher().equals(this)) {
					// FIXME: remove me
					throw new RuntimeException("WARUM????5");
				} else {
					System.err.println(this.toMyString() + "Seat found.");
				}
				return;
			} else if (currentLength < localShortestQueue) {
				localShortestSeat = localSeat;
				localShortestQueue = currentLength;
			}
		}
		
		/* Einmal remote durch alle Clients und Sitze nach freien Pl�tzen suchen. */
		for (IClient remoteClient : client.getAllClients()) {
			if (remoteClient.equals(client)) continue;
			for (ISeat remoteSeat : remoteClient.getSeats()) {
				final int currentLength = remoteSeat.tryToSitDown(this);
				if (currentLength < 0) {
					// der Philosoph hat sich remote hingestetzt.
					seat = remoteSeat;
					if (!remoteSeat.getPhilosopher().equals(this)) {
						// FIXME: remove me
						throw new RuntimeException("WARUM????7");
					} else {
						System.err.println(this.toMyString() + "Seat found.");
					}
					return;
				}
			}
		}
		
		if (seat != null) {
			// FIXME: remove me
			throw new RuntimeException("WARUM????8");
		}
		
		/* An lokalen Sitz mit k�rzester Schlange einreihen. */
		localShortestSeat.sitOrWait(this); // kehrt erst zur�ck, wenn er tats�chlich am Platz SITZT
		if (localShortestSeat.getPhilosopher() == null) {
			// FIXME: remove me
			throw new RuntimeException("WARUM??");
		}
		seat = localShortestSeat;
		return; // Der Philosoph sitzt zu diesem Zeitpunkt garantiert an einem Sitz.
	}

	private void eatAndStandUp() {
		final State nextState;
		checkSuspend();
		try {
			sleep(eatingTime); // isst
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

}

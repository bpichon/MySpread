import java.rmi.RemoteException;

public class Philosopher extends Thread implements
		IPhilosopher {

	/**
	 * Serial-Id
	 */
	private static final long serialVersionUID = 2139005198749593791L;

	public static final int sleepingTime = 10;

	public final int lockingTime = 10_000;
	public final int meditatingTime = 10;
	public final int eatingTime = 1000;
	
	private final int id;
	private State state;

	private final IClient client;
	private ISeat seat;
	private int eatCounter = 0;

	private boolean cancelled = false;

	public Philosopher(IClient client, int id) throws RemoteException {
		this.client = client;
		this.id = id;
		this.seat = null;
		state = State.SEARCHING;
		//UnicastRemoteObject.exportObject(this);
	}

	@Override
	public void run() {
		while (!cancelled) {
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
		/* Einmal lokal durchlaufen und nach freien Plätzen suchen. Gleichzeitig die kürzeste Schlange suchen. */ 
		ISeat localShortestSeat = null;
		int localShortestQueue = Integer.MAX_VALUE;
		for (ISeat localSeat : client.getSeats()) {
			final int currentLength = localSeat.tryToSitDown(this);
			if (currentLength < 0) {
				// der Philosoph hat sich hingestetzt.
				seat = localSeat;
				return;
			} else if (currentLength < localShortestQueue) {
				localShortestSeat = localSeat;
				localShortestQueue = currentLength;
			}
		}
		
		/* Einmal remote durch alle Clients und Sitze nach freien Plätzen suchen. */
		for (IClient remoteClient : client.getAllClients()) {
			for (ISeat remoteSeat : remoteClient.getSeats()) {
				remoteSeat.tryToSitDown(this);
			}
		}
		
		/* An lokalen Sitz mit kürzester Schlange einreihen. */
		localShortestSeat.sitOrWait(this); // kehrt erst zurück, wenn er tatsächlich am Platz SITZT
		seat = localShortestSeat;
		return; // Der Philosoph sitzt zu diesem Zeitpunkt garantiert an einem Sitz.
	}

	private void eatAndStandUp() {
		final State nextState;		
		
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

	@Override
	public IClient getClient() throws RemoteException {
		return client;
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
	
	static enum State {
		MEDITATING, SLEEPING, SEARCHING, /*WAITING,*/SITTING, LOCKED
	}


}

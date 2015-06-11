import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Client extends UnicastRemoteObject implements IClient {

	/**
	 * Serial-Id
	 */
	private static final long serialVersionUID = -4568230290539841571L;

	public IServer server;
	
	private TableMaster tableMaster;

	private ArrayList<IClient> allClients = new ArrayList<>();
	private ArrayList<IPhilosopher> philosophers = new ArrayList<>();
	private ArrayList<IPhilosopher> suspendedPhilosophers = new ArrayList<>();
	private ArrayList<ISeat> seats = new ArrayList<>();
	private ArrayList<IFork> forks = new ArrayList<>();

	private int id;

	// Anpassung auf die übergebene Anzahl an Philosphen und Plätzen
	public Client(int id, int philosopherAmount, int seatAmount)
			throws RemoteException {
		super();
		this.id = id;
		for (int i = 0; i < philosopherAmount; i++) {
			philosophers.add(new Philosopher(this, i));
		}
		
		for (int i = 0; i < seatAmount; i++) {
			forks.add(new Fork(this, i));
		}
		
		for (int i = 0; i < seatAmount; i++) {
			// Der letzte Seat bekommt als rechte Gabel eine null. Diese wird später in registerClient noch umgebogen.
			seats.add((ISeat) new Seat(this, forks.get(i), ((i + 1) == seatAmount) ? null : forks.get(i + 1) ,i));
		}
		
		tableMaster = new TableMaster(this);
	};

	/**
	 * 
	 * @param args
	 *            [0] - ServerAdresse | [1] - ClientId (int) | [2] - PhilosophenAnzahl | [3] -
	 *            SitzAnzahl
	 * @throws RemoteException
	 * @throws AlreadyBoundException
	 * @throws NotBoundException
	 */
	public static void main(String... args) throws RemoteException,
			AlreadyBoundException, NotBoundException {
		Registry registry;
		try {
			String host = args[0];
			int port = 9030;
			registry = LocateRegistry.getRegistry(host, port);
			IServer server = (IServer) registry.lookup("server");

			ArrayList<Client> clients = new ArrayList<>();
			final Client client = new Client(Integer.parseInt(args[1]),
					Integer.parseInt(args[2]), Integer.parseInt(args[3]));
			clients.add(client);
			client.server = server;
			if (server.registerClient(client)) {
				client.start();
				System.out.println("connected");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void clearClients() throws RemoteException {
		allClients.clear();
	}
	
	@Override
	public void addClient(IClient client) throws RemoteException {
		allClients.add(client);
	}
	
	@Override
	public void updateClients()
			throws RemoteException {

		// Gabeln umbiegen
		int indexRightNeighbor = (allClients.indexOf(this) + 1)
				% allClients.size();
		System.out.println("der gewählte rechte Nachbar ist "
				+ allClients.get(indexRightNeighbor).toMyString());
		System.err.println("neue Anzahl: " + allClients.size());
		
		// Linke Gabel des rechten Nachbarns als rechte des letzten Sitzes verwenden
		IFork leftForkOfRightNeighbor = allClients.get(indexRightNeighbor).getSeat(0).getLeftFork(); // Linkester Sitz
		getSeat(getSeats().size() - 1).setRightFork(leftForkOfRightNeighbor);
		System.out.println("finished Update Client");
	}

	@Override
	public void start() throws RemoteException {
		for (IPhilosopher philosopher : philosophers) {
			if (philosopher.getState() == Thread.State.NEW) {
				philosopher.start();
			}
		}
		
		tableMaster.start();
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public ArrayList<ISeat> getSeats() throws RemoteException {
		return seats;
	}

	@Override
	public ArrayList<IClient> getAllClients() throws RemoteException {
		return allClients;
	}

	@Override
	public void pause() throws RemoteException {
		suspendedPhilosophers.clear();
		for (IPhilosopher philosopher : philosophers) {
			if (philosopher.isAlive()) {
				philosopher.setSuspended(true);
			}
		}
	}

	@Override
	public void resume() throws RemoteException {
		for (IPhilosopher philosopher : philosophers) {
			philosopher.setSuspended(false);
		}
		for (IPhilosopher suspendedPhilosopher : suspendedPhilosophers) {
			synchronized (suspendedPhilosopher) {
				suspendedPhilosopher.notify();
			}
		}
	}
	
	@Override
	public void addSuspendedPhilosopher(IPhilosopher philosopher) throws InterruptedException, RemoteException {
		synchronized (philosopher) {
			suspendedPhilosophers.add(philosopher);
			philosopher.wait();
		}
	}

	@Override
	public String toString() {
		return "Cl(" + id + ")";
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof IClient) {
			try {
				return this.getId() == ((IClient) other).getId();
			} catch (RemoteException e) {
				return false;
			}
		}
		return false;
	}
	
	@Override
	public String toMyString() throws RemoteException {
		return toString();
	}

	@Override
	public ISeat getSeat(int i) throws RemoteException {
		return seats.get(i);
	}

	@Override
	public ArrayList<IPhilosopher> getPhilosophers() throws RemoteException {
		return philosophers;
	}

	@Override
	public ITableMaster getTableMaster() throws RemoteException {
		return tableMaster;
	}

	@Override
	public void lock(IPhilosopher currentPhilosopher) throws RemoteException {
		// FIXME: kann sein, dass das hier keine Referenz, sondern nur eine Kopie ist (von currPhiloso)
		if (!currentPhilosopher.isLocked()) {
			currentPhilosopher.lock();
		}
	}
	
	/**
	 * Fügt einen neuen Stuhl und Gabeln ein. Verbiegt die entsprechenden Gabeln auch
	 * @throws RemoteException
	 */
	@Override
	public void addSeat() throws RemoteException {
		// Es wird "rechts" eingefügt.
		final ISeat leftNeighbor = seats.get(seats.size() - 1); // Rechtester Sitz -> Linker Nachbar des neuen Sitzes
		final IFork newLeftFork = new Fork(this, forks.size());
		final IFork oldRightFork = leftNeighbor.getRightFork();
		forks.add(newLeftFork);
		leftNeighbor.setRightFork(newLeftFork);
		seats.add(new Seat(this, newLeftFork, oldRightFork, seats.size()));
	} 
	
	@Override
	public void addPhilospher(int eatCount, int eatingTimeFactor) throws RemoteException {
		philosophers.add(new Philosopher(this, philosophers.size(), eatingTimeFactor, eatCount));
	}
}

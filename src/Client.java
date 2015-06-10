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

	private ArrayList<IClient> allClients = new ArrayList<>();
	private ArrayList<IPhilosopher> philosophers = new ArrayList<>();
	private ArrayList<IPhilosopher> suspendedPhilosophers = new ArrayList<>();
	private ArrayList<ISeat> seats = new ArrayList<>();

	private int id;

	// Anpassung auf die �bergebene Anzahl an Philosphen und Pl�tzen
	public Client(int id, int philosopherAmount, int seatAmount)
			throws RemoteException {
		this.id = id;
		for (int i = 0; i < philosopherAmount; i++) {
			philosophers.add(new Philosopher(this, i));
		}
		// TODO: seats erstellen
		for (int i = 0; i < seatAmount; i++) {
			seats.add((ISeat) new Seat(this, i));
		}
		// TODO: gabeln erstellen
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
		/*int indexRightNeighbor = (allClients.indexOf(this) + 1)
				% allClients.size();
		System.out.println("der gew�hlte rechte Nachbar ist "
				+ allClients.get(indexRightNeighbor));
		System.err.println("neue Anzahl: " + clients.length);*/
		// TODO: Linke Gabel des rechten Nachbarns als rechte des letzten Sitzes verwenden 
		
	}

	@Override
	public void start() throws RemoteException {
		for (IPhilosopher philosopher : philosophers) {
			philosopher.start();
		}
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
}

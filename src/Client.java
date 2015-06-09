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
	private ArrayList<ISeat> seats = new ArrayList<>();

	private int id;

	// Anpassung auf die übergebene Anzahl an Philosphen und Plätzen
	public Client(int id, int philosopherAmount, int seatAmount)
			throws RemoteException {
		this.id = id;
		for (int i = 0; i < philosopherAmount; i++) {
			philosophers.add(new Philosopher(this, i));
		}
		// TODO: seats erstellen
		for (int i = 0; i < seatAmount; i++) {
			seats.add(new Seat(this, i));
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
	public void updateClients(ArrayList<IClient> clients)
			throws RemoteException {

		// Gabeln umbiegen
		int indexRightNeighbor = (allClients.indexOf(this) + 1)
				% allClients.size();
		System.out.println("der gewählte rechte Nachbar ist "
				+ allClients.get(indexRightNeighbor));
		
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
	public ArrayList<ISeat> getSeats() throws RemoteException {
		return seats;
	}

	@Override
	public ArrayList<IClient> getAllClients() throws RemoteException {
		return allClients;
	}

}

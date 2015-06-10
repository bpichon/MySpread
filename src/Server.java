import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

public class Server extends UnicastRemoteObject implements IServer {

	/**
	 * Serial-Id
	 */
	private static final long serialVersionUID = 7217773203069485717L;

	protected Server() throws RemoteException {
		super();
	}

	ArrayList<IClient> connectedClients = new ArrayList<>();

	public static void main(String... args) throws RemoteException {
		Registry registry;
		try {
			System.out.println("create RemoteObject");
			IServer stub = new Server();

			LocateRegistry.createRegistry(9030);
			registry = LocateRegistry.getRegistry(9030);
			registry.rebind("server", stub);

			System.out.println("-- server ready, waiting for clients --");

			System.out.println("stop server ?");
			Scanner sc = new Scanner(System.in);
			String s = sc.next();
			if (s.toLowerCase().equals("j")) {
				registry.unbind("server");
				sc.close();
				System.exit(0);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Registriert den Client im Server.
	 * 
	 * @param client
	 *            zu registrierender Client
	 * @return false, wenn bereits registriert.
	 * @throws RemoteException
	 */
	public boolean registerClient(IClient client) throws RemoteException {
		// temporär geändert um nicht immer den Server neu starten zu müssen
		if (!connectedClients.contains(client)) {
			connectedClients.add(client);
			System.out.println("new Client added");
		} else {
			connectedClients.remove(client);
			connectedClients.add(client);
			System.out.println("old Client added");
		}

		// Alle anhalten
		for (IClient eachClient : connectedClients) {
			eachClient.pause();
		}
		System.out.println("all Clients paused");
		for (IClient eachClient : connectedClients) {
			for (IClient paramClient : connectedClients) {
				eachClient.addClient(paramClient);
			}
			eachClient.updateClients();
		}
		
		
		System.out.println("all Clients updated");
		for (IClient eachClient : connectedClients) {
			eachClient.resume();
		}
		
		System.out.println("all Clients resumed");
		return true;
	}
}

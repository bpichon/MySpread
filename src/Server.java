import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Server extends UnicastRemoteObject implements IServer {

	/**
	 * Serial-Id
	 */
	private static final long serialVersionUID = 7217773203069485717L;

	public Map<Integer, IClientStats> allStats = new TreeMap<>();
	ArrayList<IClient> connectedClients = new ArrayList<>();
	
	private SuperTableMaster superTableMaster;
	
	protected Server() throws RemoteException {
		super();
		superTableMaster = new SuperTableMaster();
		superTableMaster.start();
	}

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
		// tempor�r ge�ndert um nicht immer den Server neu starten zu m�ssen
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
		client.clearClients();
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
	

	private class SuperTableMaster extends Thread {

		private static final int interval = 1000;

		public SuperTableMaster() {
		}

		public void collectStats() throws RemoteException {
			allStats.clear();
			for (IClient client : connectedClients) {
				allStats.put(client.getId(), client.getTableMaster().getStats());
			}
		}
		
		private int calculate() throws RemoteException {
			int globalMinimum = Integer.MAX_VALUE;
			for (IClientStats stats : allStats.values()) {
				for (Integer value : stats.getEatingCount()) {
					globalMinimum = (value < globalMinimum) ? value : globalMinimum;
				}
			}
			return globalMinimum;
		}
		
		private void feedbackClients(int min) throws RemoteException {
			final int greatedAllowedCount = min + 10; // TODO: Konstante
			System.err.println("greatedAllowedCount: " + greatedAllowedCount);
			for (IClientStats stats : allStats.values()) {
				for (int i = 0; i < stats.getPhilosophers().size(); i++) {
					final IPhilosopher currentPhilosopher = stats.getPhilosophers().get(i);
					final Integer currentEatingCount = stats.getEatingCount().get(i);
					System.err.println(currentPhilosopher.toMyString() + ": \t" + currentEatingCount);
					if (currentEatingCount > greatedAllowedCount) {
						System.out.println("locked " + currentPhilosopher.toMyString());
						stats.getClient().lock(currentPhilosopher);
					}
				}
			}
		}
		
		@Override
		public void run() {
			System.out.println("supertablemaster runs");
			while (!isInterrupted()) {
				try {
					sleep(interval);
					collectStats();
					int minmax = calculate();
					feedbackClients(minmax);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
					// Client abgeschmiert.
					// System.out.println(client.getId() + " Client abgeschmiert");
				}
			}
		}
	}
}

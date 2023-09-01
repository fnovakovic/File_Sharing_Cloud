package cli;


import app.AppConfig;
import app.Cancellable;
import cli.command.*;
import mutex.DistributedMutex;
import mutex.SuzukiTokenMutex;
import servent.SimpleServentListener;


import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A simple CLI parser. Each command has a name and arbitrary arguments.
 * 
 * Currently supported commands:
 * 
 * <ul>
 * <li><code>info</code> - prints information about the current node</li>
 * <li><code>pause [ms]</code> - pauses exection given number of ms - useful when scripting</li>
 * <li><code>ping [id]</code> - sends a PING message to node [id] </li>
 * <li><code>broadcast [text]</code> - broadcasts the given text to all nodes</li>
 * <li><code>causal_broadcast [text]</code> - causally broadcasts the given text to all nodes</li>
 * <li><code>print_causal</code> - prints all received causal broadcast messages</li>
 * <li><code>stop</code> - stops the servent and program finishes</li>
 * </ul>
 * 
 * @author bmilojkovic
 *
 */
public class CLIParser implements Runnable, Cancellable {

	private volatile boolean working = true;
	private volatile Queue<String> tokenWant;
	private volatile DistributedMutex mutex;
	private final List<CLICommand> commandList;
	
	public CLIParser(SimpleServentListener listener, DistributedMutex distributedMutex) {
		this.commandList = new ArrayList<>();
		this.mutex = distributedMutex;
		this.tokenWant = new ConcurrentLinkedQueue<>();
		commandList.add(new PauseCommand());
		commandList.add(new InitToken(mutex));
		commandList.add(new GetCommand((SuzukiTokenMutex) distributedMutex));
		commandList.add(new AddCommand((SuzukiTokenMutex) distributedMutex,tokenWant));
		commandList.add(new RemoveCommand((SuzukiTokenMutex) distributedMutex));
		commandList.add(new StopCommand(this, listener));
	}
	
	@Override
	public void run() {
		Scanner sc = new Scanner(System.in);
		AppConfig.timestampedErrorPrint("MUTEX JE " + mutex );
		while (working) {
			String commandLine = sc.nextLine();
			
			int spacePos = commandLine.indexOf(" ");
			
			String commandName = null;
			String commandArgs = null;
			if (spacePos != -1) {
				commandName = commandLine.substring(0, spacePos);
				commandArgs = commandLine.substring(spacePos+1, commandLine.length());
			} else {
				commandName = commandLine;
			}
			
			boolean found = false;
			
			for (CLICommand cliCommand : commandList) {
				if (cliCommand.commandName().equals(commandName)) {
					cliCommand.execute(commandArgs);
					found = true;
					break;
				}
			}
			
			if (!found) {
				AppConfig.timestampedErrorPrint("Unknown command: " + commandName);
			}
		}
		
		sc.close();
	}
	
	@Override
	public void stop() {
		this.working = false;
		
	}
}

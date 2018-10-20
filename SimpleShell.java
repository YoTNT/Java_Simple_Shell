import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.nio.file.Paths;
import java.lang.*;

public class SimpleShell {

	public static void main(String[] args) throws java.io.IOException
	{
		String commandLine;
		boolean isWindows = System.getProperty("os.name")
			.toLowerCase().startsWith("windows");
		boolean init = false;
		Stack<String> currentRelativeFolder = new Stack<String>();
		File folderRecord = new File(System.getProperty("user.dir"));
		List<String> command_history = new ArrayList<String>();
		BufferedReader console = new BufferedReader
				(new InputStreamReader(System.in));
		
		System.out.println("Welcome to simple shell. Type help to get more information.");
		
		while(true)
		{
			System.out.print("jsh>");
			commandLine = console.readLine();
			
			if(commandLine.equals(""))
				continue;
			
			if(commandLine.charAt(0) != '!')
				command_history.add(commandLine);
			// renew command size
			
			// parse the input to obtain the command and any parameters
			List<String> command = new ArrayList<String>();
/*
			// identify OS to specify commandc
			if(isWindows)
			{
				command.add("cmd.exe");
				command.add("/c");
			}
			else
			{
				command.add("sh");
				command.add("-c");
			}
*/
			// check if '!' is used
			if(commandLine.charAt(0) == '!')
			{
				if(commandLine.equals("!!"))
				{
					int last_index = command_history.size() - 1;
					if(last_index < 0)
					{
						System.out.println("There's no previous command!");
						continue;
					}
					else
					{
						commandLine = command_history.get(last_index);
						// record this command to the history
						command_history.add(commandLine);
					}
				}
				else
				{
					int commandIndex = 0;
					int power = 1;
					int commandLine_length = commandLine.length();
					// scan every char after '!' to see if it's legal command
					for(int i = commandLine_length - 1; i >= 1; i--)
					{
						if(Character.isDigit(commandLine.charAt(i)))
						{
							int digit = (int)commandLine.charAt(i) - 48;
							int number = digit * power;
							commandIndex += number;
							power *= 10;
						}
						else
						{
							System.out.println("Invalid command. '!' must be followed by <int> only!");
						}
					}
					// command index is ready
					// but need to check to see if it's legal!
					if(commandIndex >= command_history.size())
					{
						System.out.println("There's no such index of command in history!");
						continue;
					}
					else
					{
						commandLine = command_history.get(commandIndex);
						command_history.add(commandLine);
					}
				}
			}
			
			if(commandLine.equals("history"))
			{
				for(int i = 0; i < command_history.size(); i++)
					System.out.println(i + " " + command_history.get(i));
				continue;
			}

			
			String[] components = commandLine.split("\\s");
			for(int i = 0; i < components.length; i++)
				command.add(components[i]);

			// create a ProcessBuilder object
			ProcessBuilder build = new ProcessBuilder(command);
			build.directory(folderRecord);
			System.out.println("Command: " + build.command());
			
			// change directory
			if(command.get(0).equals("cd"))
			{
				if(command.size() == 1)	// home directory
				{
					folderRecord = new File(System.getProperty("user.dir"));
					currentRelativeFolder.clear();		// clean up the folder stack completely
					build.directory(folderRecord);
					System.out.println(build.directory());		
					continue;
				}
				else if(command.size() == 2 && !command.get(1).equals(".."))
				{
					File absolute_folder = new File(build.directory().toString(), command.get(1));
					if(!absolute_folder.exists() || !absolute_folder.isDirectory())
					{
						System.out.println("Invalid folder direction!");
						continue;
					}
					else
					{
						currentRelativeFolder.push(command.get(1));		// record the correct current relative folder name
						// Change current directory
						folderRecord = absolute_folder;
						build.directory(folderRecord);
						System.out.println(build.directory());
						continue;
					}
				}
				else if(command.size() == 2 && command.get(1).equals(".."))
				{
					if(currentRelativeFolder.isEmpty())
					{
						System.out.println("Illegal operation! You can't jump out of the home directory!");
					}
					else
					{
						String currentPath = build.directory().toString();
						String currentFolder = currentRelativeFolder.pop();
						currentPath = currentPath.substring(0, currentPath.length() - currentFolder.length() - 1);
						folderRecord = new File(currentPath);	// renew the folder record
						build.directory(folderRecord);
						System.out.println(build.directory());
					}
				}
				else
				{
					System.out.println("Invalid command. Acessing folder: cd <folder name>. Going up: cd ..(2 dots only).");
					continue;
				}
			}

			try 
			{
				// start the process
				if(command.size() == 2)
					if(command.get(1).equals(".."))
						continue;
				Process p = build.start();
			
				// obtain the output stream
				InputStream is = p.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
			
				// output the contents returned by the command */
				String line;
				while( (line = br.readLine()) != null)
					System.out.println(line);
			
				br.close();
			}catch(java.io.IOException e)
			{
				System.out.println("Invalid command!");
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
